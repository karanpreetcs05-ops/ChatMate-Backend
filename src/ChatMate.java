import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import okhttp3.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ChatMate {
    // Keep your NVIDIA key here
    private static final String API_KEY = "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"; 
    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        // CLOUD FIX: Render/Heroku dynamic port detection
        String portStr = System.getenv("PORT");
        int port = (portStr != null) ? Integer.parseInt(portStr) : 8080;

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/chat", new ChatHandler());
        server.setExecutor(null);
        
        System.out.println("ChatMate AI Cloud Server started on port: " + port);
        server.start();
    }

    static class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS Headers for global access
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String userMsg = br.readLine();

            String aiReply = callNvidia(userMsg);

            byte[] response = aiReply.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    private static String callNvidia(String msg) throws IOException {
        String json = "{\"model\": \"meta/llama-3.1-405b-instruct\", \"messages\": [{\"role\": \"user\", \"content\": \"" + msg + "\"}]}";
        
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url("https://integrate.api.nvidia.com/v1/chat/completions")
            .header("Authorization", "Bearer " + API_KEY)
            .post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "{\"choices\":[{\"message\":{\"content\":\"Backend Error: API issue.\"}}]}";
            return response.body().string();
        }
    }
}