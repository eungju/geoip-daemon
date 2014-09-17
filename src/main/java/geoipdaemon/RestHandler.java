package geoipdaemon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

class RestHandler implements HttpHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    HttpHandler badRequest = new ResponseCodeHandler(StatusCodes.BAD_REQUEST);
    HttpHandler notFound = new ResponseCodeHandler(StatusCodes.NOT_FOUND);
    HttpHandler methodNotAllowed = new ResponseCodeHandler(StatusCodes.METHOD_NOT_ALLOWED);

    final ObjectMapper jsonMapper = new ObjectMapper();
    final Databases databases;

    public RestHandler(Databases databases) {
        this.databases = databases;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if (!Methods.GET.equals(exchange.getRequestMethod())) {
            methodNotAllowed.handleRequest(exchange);
            return;
        }

        String[] fragments = exchange.getRelativePath().replaceAll("(^/|/$)", "").split("/");
        if (fragments.length != 3 || !"databases".equals(fragments[0])) {
            notFound.handleRequest(exchange);
            return;
        }

        String dbName = fragments[1];
        String query = fragments[2];

        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(query);
        } catch (Exception e) {
            logger.warn("Invalid IP address " + query);
            badRequest.handleRequest(exchange);
            return;
        }

        JsonNode record = databases.lookup(dbName, inetAddress);
        if (record == null) {
            notFound.handleRequest(exchange);
            return;
        }

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=utf-8");
        exchange.getResponseSender().send(jsonMapper.writeValueAsString(record));
    }
}
