package de.nalch.jetbook;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;

/**
 * Utility class to generate a pdf file containing pages readable by the rocketbook app but customized
 * from some simple templates (for fancy logos and comprehensible icons).
 */
public final class JetbookMain {

  /**
   * How many pages the final product should have. Stick between 0-99 for the time being.
   */
  private static final int PAGECOUNT = 98;
  /**
   * Which template to use.
   */
  private static final String TEMPLATENAME = "nalch-logo";
  /**
   * Where to store the resulting pdf.
   */
  private static final String RESULTNAME = "results" + File.separator + TEMPLATENAME + "-result";

  /**
   * Nothing to do here, just preventing anyone to instantiate a utility class.
   */
  private JetbookMain() {
  }

  /**
   * Generate a configurable rocketbook to print.
   *
   * @param args
   *          Arguments are not supported yet. Use the members of this class
   */
  public static void main(final String[] args) {
    try {
      // read configuration
      Configurations configs = new Configurations();
      Configuration config = configs.properties(
          JetbookMain.class.getResource(
              File.separator + "templates" + File.separator + TEMPLATENAME + ".properties"
          )
      );

      File tempFile = File.createTempFile("jetbook", ".pdf");
      File tempFileStamped = File.createTempFile("jetbook_stamped", ".pdf");
      PdfReader templateReader = new PdfReader(
          JetbookMain.class.getResourceAsStream("/templates/" + TEMPLATENAME + ".pdf")
      );
      Document document = new Document();

      try {
        PdfSmartCopy tempResult = new PdfSmartCopy(document, new FileOutputStream(tempFile));
        document.open();

        for (int pageNumber = 1; pageNumber <= PAGECOUNT; pageNumber++) {
          PdfImportedPage pdfPage = tempResult.getImportedPage(templateReader, 1);
          tempResult.addPage(pdfPage);
        }

        document.close();
        templateReader.close();
        tempResult.close();

        PdfReader resultReader = new PdfReader(tempFile.getAbsolutePath());
        PdfStamper pdfStamper = new PdfStamper(
            resultReader,
            new FileOutputStream(tempFileStamped.getAbsolutePath())
        );
        for (int currentPage = 1; currentPage <= PAGECOUNT; currentPage++) {
          PdfContentByte content = pdfStamper.getOverContent(currentPage);

          // prepare QR code
          int qrCodeSize = config.getInt("qrCodeSize");
          String pageString = String.format(config.getString("qrCodePageFormat"), currentPage);
          String qrCodeText = MessageFormat.format(
              config.getString("qrCodeTextTemplate"),
              pageString
          );
          String qrapiString = "https://api.qrserver.com/v1/create-qr-code/"
              + "?size=" + qrCodeSize + "x" + qrCodeSize
              + "&ecc=Q" + "&data=" + URLEncoder.encode(qrCodeText, "UTF-8");
          Image qrCode = Image.getInstance(new URL(qrapiString));

          content.addImage(
              qrCode,
              qrCodeSize,
              0,
              0,
              qrCodeSize,
              config.getFloat("qrCodeX"),
              config.getFloat("qrCodeY")
          );
        }
        pdfStamper.close();
        resultReader.close();

        FileUtils.copyFile(tempFileStamped, new File(RESULTNAME + ".pdf"));
      } catch (DocumentException e) {
        e.printStackTrace();
      } finally {
        FileUtils.deleteQuietly(tempFile);
        FileUtils.deleteQuietly(tempFileStamped);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ConfigurationException e1) {
      e1.printStackTrace();
    }

    System.out.println("finished");

  }

}
