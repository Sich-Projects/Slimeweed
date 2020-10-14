package com.candlelit.slimeweed;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SeaweedFilerClient {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType MEDIA_TYPE_PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    private static final Logger logger = Logger.getLogger("SeaweedFilerClient");
    private static final Gson gson = new Gson();
    public static final int LIST_LIMIT = 100; //100 is the default

    private final String url;
    private final String host;
    private final int port;

    public SeaweedFilerClient(String host) {
        this(host, 8888); //if there's a way to change this, then it should be different.
    }
    public SeaweedFilerClient(String host, int port) {
        this.host = host;
        this.port = 8888;
        this.url = String.format("http://%s:%d/", host, port);
    }

    public String postFile(String dir, String name, byte[] data) throws IOException {
        if (dir.charAt(0) == '/') dir = dir.substring(1);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", name, RequestBody.create(MEDIA_TYPE_PLAINTEXT, data))
                .build();
        Request request = new Request.Builder()
            .url(this.url + dir + "/" + name)
            .post(requestBody)
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) logger.info("Something went wrong with the http call");
            return response.body().string();
        }
    }

    public byte[] getFile(String dir, String fileName) throws IOException {
        if (dir.charAt(0) == '/') dir = dir.substring(1);

        Request request = new Request.Builder()
            .url(this.url + dir + "/" + fileName)
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().bytes();
        }
    }

    public boolean deleteFile(String dir, String name) throws IOException {
        if (dir.charAt(0) == '/') dir = dir.substring(1);
        Request request = new Request.Builder()
            .delete()
            .url(this.url + dir + "/" + name)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string().isEmpty();
        }
    }

    public List<String> listFiles(String dir) throws IOException {
        if (dir.charAt(0) == '/') dir = dir.substring(1);

        return listHelper("", LIST_LIMIT, dir);
    }

    private List<String> listHelper(String lastFile, int limit, String dir) throws IOException {
        Request request = new Request.Builder()
                .url(this.url + dir + "?limit=" + limit +"&lastFileName=" + lastFile)
                .header("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String resp = response.body().string();
            if (resp.contains("\"Entries\":null")) return new ArrayList<>();
            JsonObject obj = gson.fromJson(resp, JsonObject.class);
            JsonArray arr = obj.getAsJsonArray("Entries");
            List<String> files = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                files.add(arr.get(i).getAsJsonObject().get("FullPath").getAsString().replace('/' + dir + '/', ""));
            }
            if (files.size() < limit) {
                return files;
            } else {
                files.addAll(listHelper(obj.get("LastFileName").getAsString(), limit, dir));
                return files;
            }
        }
    }

}
