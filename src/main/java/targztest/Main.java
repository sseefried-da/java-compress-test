package targztest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.utils.IOUtils;

public class Main {

  private static String reallyLongPath = "/this/is/a/stupidly/long/path/name/because/we/had/some/problems/earlier/where/our/path/names/were/huge/for/a/zip/file/and/it/caused/lots/of/Java/heap/to/be/used";

  private static int NUM_FILES = 1000000; // number of files to compress
  private static int PROGRESS_AT = 100000; // show progress at

  private static String reallyBigString = "";

  public static void main(String... argv) throws Exception {
    setReallyBigString();
    apacheTarGzTest();
    // zipTest();

  }

  public static void apacheTarGzTest() throws Exception {
    String fileName = "test.tar.gz";
    System.out.println("Writing to " + fileName);
    FileOutputStream fileOut = new FileOutputStream(fileName);
    BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);
    GzipParameters gzipParams = new GzipParameters();
    gzipParams.setCompressionLevel(Deflater.BEST_SPEED);
    GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut, gzipParams);
    TarArchiveOutputStream output = new TarArchiveOutputStream(gzOut);

    output.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

    System.out.print("Progress: ");
    for (int i = 0; i < NUM_FILES; i++) {
      String filePath = String.format("%s/%d.txt", reallyLongPath, i);
      TarArchiveEntry tarEntry = new TarArchiveEntry(new File(filePath), filePath);
      tarEntry.setSize(reallyBigString.length());
      output.putArchiveEntry(tarEntry);
      byte[] bytes = reallyBigString.getBytes();
      output.write(bytes, 0, bytes.length);
      output.closeArchiveEntry();
      if ((i + 1) % PROGRESS_AT == 0 ) {
        System.out.print("*");
      }
    }
    makeHeapDump();

    System.out.println("Closing the .tar.gz file");
    output.close();
  }

  public static void zipTest() throws Exception {
    String fileName = "test.zip";
    System.out.println("Writing to " + fileName);
    FileOutputStream fileOut = new FileOutputStream(fileName);
    ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fileOut));

    System.out.print("Progress: ");
    for (int i = 0; i < NUM_FILES; i++) {
      String filePath = String.format("%s/%d.txt", reallyLongPath, i);
      ZipEntry zipEntry = new ZipEntry(filePath);
      zipOut.putNextEntry(zipEntry);
      zipOut.write(reallyBigString.getBytes());
      zipOut.closeEntry();
      if ((i + 1) % PROGRESS_AT == 0 ) {
        System.out.print("*");
      }
    }

    makeHeapDump();

    System.out.println("Closing the zip file");
    zipOut.finish();
    zipOut.close();
    fileOut.close();
  }

  public static void setReallyBigString() {
    String repeatText = "Here is some text to repeat\n";
    for (int i = 0; i < 1000; i ++) {
      reallyBigString = reallyBigString.concat(repeatText);
    }
  }

  public static void makeHeapDump() throws Exception {
    Process process = Runtime.getRuntime().exec("./java-heap-dump.sh tar-gz");
    StringBuilder output = new StringBuilder();

    BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));

    String line;
    while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
    }

    process.waitFor();
    System.out.println(output);
  }

}