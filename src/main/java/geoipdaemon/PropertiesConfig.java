package geoipdaemon;

import java.util.Properties;

public class PropertiesConfig {
    Properties properties;

    public PropertiesConfig(Properties properties) {
        this.properties = properties;
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
