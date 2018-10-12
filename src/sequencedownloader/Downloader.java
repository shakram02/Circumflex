/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sequencedownloader;

import javafx.concurrent.Task;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author mhrimaz
 */
public class Downloader extends Task<Void> {

    private URL url;
    private String fileName;
    private int fileSize;

    /**
     * @param url      URL of file to download
     * @param fileName name of the file for saving
     */
    public Downloader(URL url, String fileName) {
        this.url = url;
        this.fileName = fileName;
    }

    @Override
    protected Void call() throws Exception {
        try {
            //Code to download
            URLConnection openConnection = url.openConnection();
            fileSize = openConnection.getContentLength();

            ByteArrayOutputStream out;
            try (InputStream in = new BufferedInputStream(url.openStream())) {
                out = new ByteArrayOutputStream();
                byte[] buf = new byte[5120];
                int n = 0;
                long downloaded = 0;
                while (-1 != (n = in.read(buf))) {
                    downloaded += n;
                    out.write(buf, 0, n);
                    updateProgress(downloaded, fileSize);
                    updateMessage("Downloaded : " + (downloaded / (1024F * 1024F * 8F)) + " MB");
                }
                out.close();
                System.err.println(String.format("[Download] Complete %s from %s", fileName, url.getPath()));
            }

            byte[] response = out.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(response);
                //End download code
            }
        } catch (Exception e) {
            System.err.println(String.format("[Download] Failed %s from %s", fileName, url.getPath()));
            updateProgress(0, fileSize);
            updateMessage("Download failed: " + e.getMessage());
        }
        return null;
    }

}
