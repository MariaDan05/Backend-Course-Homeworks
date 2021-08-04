package com.picsart.server;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class RequestHandler {
    private static final String FREE_SPACE = "<br><br><br>";
    private static final String HTML_START = "<html><title>Simple Java HTTP Server</title><body>";
    private static final String HTML_END = "</body></html>";
    private static final String HTML_FORM = "<form method='POST'>" +
            "<input name='parameter' type='text'/>" +
            "<input type='submit'/>" +
            "</form>";

    private static final String HOME_PAGE = "<h1>Welcome to HOME PAGE</h1><h2>This page was served using my Simple Java HTTP Server</h2>";
    private static final String MESSAGE_PAGE = "<h1>Welcome to MESSAGE PAGE</h1>";

    public static void handleRequest(Socket socket) throws IOException {
        OutputStream os = socket.getOutputStream();

        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        Request request = new Request(reader);
        if (!request.parse()) {
            respond(500, "Unable to parse request", os);
            return;
        }

        String responseHtml = null;

        if ("/message".equals(request.getPath())) {
            responseHtml = handleMessagePage(request);
        } else {
            responseHtml = handleRequestMethod(request, HOME_PAGE);
        }

        String result = getResponse(responseHtml) + responseHtml;
        os.write(result.getBytes());
        os.flush();
        reader.close();
    }

    private static String getResponse(String responseHtml) {
        return "HTTP/1.1 200 OK\r\n"
                + "Server: Server\r\n"
                + "Content-Type: text/html\r\n"
                + "Content-Length: "
                + responseHtml.length()
                + "\r\n"
                + "Connection: close\r\n\r\n";
    }

    private static void respond(int statusCode, String message, OutputStream out) throws IOException {
        String responseLine = "HTTP/1.1 " + statusCode + " " + message + "\r\n\r\n";
        out.write(responseLine.getBytes());
    }

    private static String handleMessagePage(Request request) throws IOException {
        return handleRequestMethod(request, MESSAGE_PAGE);
    }

    private static String handleRequestMethod(Request request, String pageTitle) throws IOException {
        StringBuilder generatedRequestHtml = new StringBuilder();

        generatedRequestHtml.append(HTML_START);

        if (request.getMethod().equals("POST")) {

            BufferedReader reader = request.getInputReader();

            String payload = "<div><h1>Payload DATA from POST = </h1>" + getPostPayloadData(reader) + "</div>" + FREE_SPACE;

            generatedRequestHtml.append(pageTitle).append(FREE_SPACE);
            generatedRequestHtml.append(payload);

            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                generatedRequestHtml.append("<p style='margin: 0; padding: 0;'><b>").append(key)
                        .append(": </b>").append(value).append("</p>");
            }
            generatedRequestHtml.append(HTML_END);
        } else {
            generatedRequestHtml.append(pageTitle).append(FREE_SPACE);
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                generatedRequestHtml.append("<p style='margin: 0; padding: 0;'><b>").append(key)
                        .append(": </b>").append(value).append("</p>");
            }
            generatedRequestHtml.append(FREE_SPACE + HTML_FORM + HTML_END);
        }

        return String.valueOf(generatedRequestHtml);
    }

    private static String getPostPayloadData(BufferedReader bufferedReader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        char[] charBuffer = new char[128];
        int bytesRead = -1;

        bytesRead = bufferedReader.read(charBuffer);
        stringBuilder.append(charBuffer, 0, bytesRead);

        return stringBuilder.toString();
    }
}
