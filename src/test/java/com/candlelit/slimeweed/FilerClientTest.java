package com.candlelit.slimeweed;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class FilerClientTest {
    private static SeaweedFilerClient client;
    private static byte[] byteTest;
    private static final String path = "/com/candlelit/slimeweed/test";
    private static final String fileName = "articuno.png";
    @BeforeAll
    public static void before() {
        client = new SeaweedFilerClient("localhost");

        BufferedImage image = null;

        try {
            URL url = new URL("https://heavy.com/wp-content/uploads/2016/07/red_articuno_po-e1470145578952.jpg?quality=65&strip=all&w=1350");
            image = ImageIO.read(url);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.out.println("Successfully downloaded some picture, now converting to byte array...");
            ImageIO.write(image, "jpg", output);
            byteTest = output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void post() {
        System.out.println("Testing posting..");
        try {
            String response = client.postFile(path, fileName, byteTest);
            Assertions.assertEquals(response, "{\"name\":\"" + fileName + "\",\"size\":" + byteTest.length + "}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void get() {
        System.out.println("Testing getting..");
        try {
            client.postFile(path, fileName, byteTest);
            byte[] bytes = client.getFile(path, fileName);
            Assertions.assertEquals(byteTest.length, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delete() {
        System.out.println("Testing deleting..");
        try {
            client.postFile(path, fileName + 2, byteTest);
            boolean response = client.deleteFile(path, fileName + 2);
            Assertions.assertTrue(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void list() {
        System.out.println("Testing listing..");
        try {
            int size = 10;
            for (int i = 0; i < size; i++)
                client.postFile(path, fileName + i, byteTest);
            List<String> list = client.listFiles(path);
            Assertions.assertTrue(list.size() >= size);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void cleanup() {
        try {
            List<String> list = client.listFiles(path);
            for (String li : list)
                client.deleteFile(path, li);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
