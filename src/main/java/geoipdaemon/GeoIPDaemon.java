package geoipdaemon;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.base.Joiner;
import com.maxmind.db.Reader;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class GeoIPDaemon implements Daemon {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    PropertiesConfig config;
    Databases databases;
    Undertow webServer;

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        Properties properties = new Properties();
        properties.load(new FileReader(daemonContext.getArguments()[0]));
        config = new PropertiesConfig(properties);

        configureLogger();
        databases = buildDatabases();
        webServer = buildWebServer(databases);
    }

    @Override
    public void start() throws Exception {
        webServer.start();
    }

    @Override
    public void stop() throws Exception {
        webServer.stop();
    }

    @Override
    public void destroy() {
        webServer = null;
        databases = null;
        config = null;
    }

    void configureLogger() {
        String confPath = config.getLoggerConf();
        // assume SLF4J is bound to logback in the current environment
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            loggerContext.reset();
            configurator.doConfigure(confPath);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
    }

    Databases buildDatabases() throws Exception {
        String[] names = config.getDatabaseNames();
        logger.info("Building databases {}", Joiner.on(",").join(names));
        Databases databases = new Databases();
        for (String name : names) {
            String path = config.getDatabasePath(name);
            logger.info("Loading database {} from {}", name, path);
            Reader db = new Reader(new File(path));
            databases.add(name, db);
        }
        return databases;
    }

    Undertow buildWebServer(Databases databases) throws Exception {
        int port = config.getWebPort();
        logger.info("Building web server with port {}.", port);
        RestHandler restHandler = new RestHandler(databases);
        PathHandler pathHandler = new PathHandler();
        pathHandler.addPrefixPath("/rest", restHandler);
        HttpHandler rootHandler = new SimpleErrorPageHandler(pathHandler);
        return Undertow.builder()
                .addHttpListener(port, null)
                .setHandler(rootHandler).build();
    }

    public static void main(final String[] args) throws Exception {
        GeoIPDaemon daemon = new GeoIPDaemon();
        daemon.init(new DaemonContext() {
            @Override
            public DaemonController getController() {
                return null;
            }

            @Override
            public String[] getArguments() {
                return new String[] {"src/test/resources/geoip-daemon.properties"};
            }
        });
        daemon.start();
    }
}
