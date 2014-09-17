package geoipdaemon;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertiesConfig {
    Properties properties;

    public PropertiesConfig(String name) throws IOException {
        properties = new Properties();
        properties.load(new FileReader(name));
    }

    public String getLoggerConf() {
        return properties.getProperty("logger.conf", "conf/logback.xml");
    }

    public String[] getDatabaseNames() {
        return properties.getProperty("databases").split(",");
    }

    public String getDatabasePath(String name) {
        return properties.getProperty("databases." + name);
    }

    public int getWebPort() {
        return Integer.parseInt(properties.getProperty("web.port", "4000"));
    }
}
