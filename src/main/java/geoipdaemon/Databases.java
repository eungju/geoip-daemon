package geoipdaemon;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxmind.db.Reader;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Databases {
    private final Map<String, Reader> databases = new HashMap<>();

    public void add(String name, Reader database) {
        databases.put(name, database);
    }

    public JsonNode lookup(String name, InetAddress address) throws IOException {
        Reader reader = databases.get(name);
        if (reader == null) {
            return null;
        }
        return reader.get(address);
    }
}
