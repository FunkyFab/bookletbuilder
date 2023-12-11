package fr.fb.bookletbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class BookletBuilder {

	public static void main(String[] args) throws IOException {
		new BookletBuilder(args[0]);
	}

	public BookletBuilder(String configFile) throws IOException {
		loadProperties(configFile);
		PDDocument document = createDocument();
		processFiles(document);
		writeDocument(document);
	}

	private void writeDocument(PDDocument document) throws IOException {
		document.save(props.getProperty("booklet.path"));
		document.close();
	}

	private PDDocument createDocument() {
		PDDocument document = new PDDocument();
		PDDocumentInformation pdd = document.getDocumentInformation();
		String author = props.getProperty("booklet.author");
		if (author != null)
			pdd.setAuthor(author);
		String title = props.getProperty("booklet.title");
		if (title != null)
			pdd.setTitle(title);
		String creator = props.getProperty("booklet.creator");
		if (creator != null)
			pdd.setCreator(creator);
		String subject = props.getProperty("booklet.subject");
		if (subject != null)
			pdd.setSubject(subject);
		Calendar cal = Calendar.getInstance();
		pdd.setCreationDate(cal);
		pdd.setModificationDate(cal);
		String keywords = props.getProperty("booklet.keywords");
		if (keywords != null) {
			pdd.setKeywords(keywords);
		}
		return document;
	}

	private void processFiles(PDDocument document) throws IOException {
		int fileIndex = 1;
		boolean finished = false;
		PDFCloneUtility cloner = new PDFCloneUtility(document);
		do {
			String currentPath = props.getProperty("score." + fileIndex + ".path");
			if (currentPath != null) {
				System.out.println(currentPath);
				// Insert blank page
				if (currentPath.equals("blank")) {
					PDPage scorePage = new PDPage(PDRectangle.A4);
					document.addPage(scorePage);
				} else {
					try {
						// PDF files are handled differently
						if (!currentPath.contains(".pdf")) {
							pageFromImage(document, fileIndex, currentPath);
						} else {
							pageFromPDF(document, fileIndex, cloner, currentPath);
						}
					} catch (Exception e) {
						System.err.println("Error processing " + currentPath);
						e.printStackTrace();
					}
				}
				fileIndex++;
			} else {
				finished = true;
			}
		} while (!finished);
	}

	private void pageFromPDF(PDDocument document, int fileIndex, PDFCloneUtility cloner, String currentPath)
			throws IOException {
		// One pagers only
		File file = new File(currentPath);
		PDDocument score = PDDocument.load(file);
		PDPage srcPage = score.getPage(0);
		COSDictionary pageDictionary = (COSDictionary) cloner.cloneForNewDocument(srcPage);
		PDPage dstPage = new PDPage(pageDictionary);
		dstPage.setRotation(90 * getRotation(fileIndex));
		document.addPage(dstPage);
		score.close();
	}

	private void pageFromImage(PDDocument document, int fileIndex, String currentPath) throws IOException {
		PDPage scorePage = new PDPage(PDRectangle.A4);
		document.addPage(scorePage);
		PDPageContentStream contents = new PDPageContentStream(document, scorePage);
		// Need to resize/rotate
		int rotation = getRotation(fileIndex);
		ImageUtil util = new ImageUtil(new File(currentPath), rotation, WIDTH_MAX, HEIGHT_MAX);
		PDImageXObject pdImage = PDImageXObject.createFromFileByContent(util.getResult(), document);
		contents.drawImage(pdImage, 0, 0);
		contents.close();
		util.getResult().delete();
	}

	private void loadProperties(String configFile) throws IOException {
		props = new Properties();
		InputStream in = new FileInputStream(configFile);
		props.load(in);
		in.close();

	}

	private int getRotation(int fileIndex) {
		String strRotation = props.getProperty("score." + fileIndex + ".rotation");
		int rotation = 0;
		if (strRotation != null) {
			rotation = Integer.parseInt(strRotation);
		}
		return rotation;
	}

	private Properties props;
	private final static int DEFAULT_DPI = 72;
	private final static float INCH_IN_CM = 2.54f;
	private final static int WIDTH_MAX = (int) (21 / INCH_IN_CM * DEFAULT_DPI);
	private final static int HEIGHT_MAX = (int) (29.7 / INCH_IN_CM * DEFAULT_DPI);
}
