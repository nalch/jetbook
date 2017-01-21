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
import java.util.function.Consumer;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Utility class to generate a pdf file containing pages readable by the
 * rocketbook app but customized from some simple templates (for fancy logos and
 * comprehensible icons).
 */
public final class JetbookMain {

  /**
   * Nothing to do here, just preventing anyone to instantiate a utility class.
   */
  private JetbookMain() {
  }

  /**
   * Generate a configurable rocketbook to print.
   *
   * @param args
   *          program argument. Use --help to show usage
   */
  public static void main(final String[] args) {

    // configure command line arguments
    OptionParser parser = new OptionParser(); // niov:
    OptionSpec<Integer> optionPagecount = parser
        .accepts("n")
        .withOptionalArg()
        .ofType(Integer.class)
        .defaultsTo(1);
    OptionSpec<String> optionTemplatename = parser
        .accepts("i")
        .withOptionalArg()
        .ofType(String.class)
        .defaultsTo("nalch-default");
    OptionSpec<String> optionResultname = parser
        .accepts("o")
        .withOptionalArg()
        .ofType(String.class)
        .defaultsTo("results" + File.separator + "result");
    parser.accepts("q");
    OptionSet options = parser.parse(args);

    // How many pages the final product should have
    final int pageCount = options.valueOf(optionPagecount).intValue();
    // Which template to use
    final String templateName = options.valueOf(optionTemplatename);
    // Where to store the resulting pdf
    final String resultName = options.valueOf(optionResultname);
    final boolean quiet = options.has("q");

    // create log functions, that only log, if the program is not quiet
    //CHECKSTYLE.OFF: Inline Conditionals
    // sufficiently simple to understand, so checkstyle does not have power here
    Consumer<String> log = quiet ? (message) -> { } : System.out::print;
    Consumer<String> logln = quiet ? (message) -> { } : System.out::println;
    //CHECKSTYLE.ON: Inline Conditionals

    logln.accept("Starting generation");

    try {
      // read configuration
      Configurations configs = new Configurations();
      Configuration config = configs.properties(
          JetbookMain.class.getResource(File.separator + "templates" + File.separator + templateName + ".properties")
      );

      final int maxPages = config.getInt("maxPageCount", Integer.MAX_VALUE);
      if (pageCount > maxPages) {
        throw new IllegalArgumentException("A book with over " + maxPages + "pages is not supported by this template");
      }

      File tempFile = File.createTempFile("jetbook", ".pdf");
      File tempFileStamped = File.createTempFile("jetbook_stamped", ".pdf");
      PdfReader templateReader = new PdfReader(
          JetbookMain.class.getResourceAsStream("/templates/" + templateName + ".pdf")
      );
      Document document = new Document();

      try {
        log.accept("Populating temporary pdf with " + pageCount + " pages");
        PdfSmartCopy tempResult = new PdfSmartCopy(document, new FileOutputStream(tempFile));
        document.open();

        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
          PdfImportedPage pdfPage = tempResult.getImportedPage(templateReader, 1);
          tempResult.addPage(pdfPage);
          log.accept(".");
        }

        document.close();
        templateReader.close();
        tempResult.close();

        log.accept("\nPopulating temporary pdf with qrcodes");
        PdfReader resultReader = new PdfReader(tempFile.getAbsolutePath());
        PdfStamper pdfStamper = new PdfStamper(resultReader, new FileOutputStream(tempFileStamped.getAbsolutePath()));
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
          PdfContentByte content = pdfStamper.getOverContent(currentPage);

          // prepare QR code
          int qrCodeSize = config.getInt("qrCodeSize");
          String pageString = String.format(config.getString("qrCodePageFormat"), currentPage);
          String qrCodeText = MessageFormat.format(config.getString("qrCodeTextTemplate"), pageString);
          String qrapiString = "https://api.qrserver.com/v1/create-qr-code/"
              + "?size=" + qrCodeSize + "x" + qrCodeSize
              + "&ecc=Q"
              + "&data=" + URLEncoder.encode(qrCodeText, "UTF-8");
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
          log.accept(".");
        }
        pdfStamper.close();
        resultReader.close();

        logln.accept("\nMoving result to " + resultName + ".pdf");
        FileUtils.copyFile(tempFileStamped, new File(resultName + ".pdf"));
      } catch (DocumentException e) {
        e.printStackTrace();
      } finally {
        logln.accept("Delete temporary files");
        FileUtils.deleteQuietly(tempFile);
        FileUtils.deleteQuietly(tempFileStamped);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ConfigurationException e1) {
      e1.printStackTrace();
    }

    logln.accept("finished");

  }

}
