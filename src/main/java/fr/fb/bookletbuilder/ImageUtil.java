package fr.fb.bookletbuilder;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class ImageUtil {
	public ImageUtil(File imagePath, int cwRotations, int width, int height) throws IOException {
		BufferedImage current = ImageIO.read(imagePath);
		// Rotation
		for (int i = 0; i < cwRotations; i++) {
			current = rotateCw(current);
		}
		// Scaling
		float resizeX = width/(1.0f*current.getWidth());
		float resizeY = height/(1.0f*current.getHeight());
		float resize = Math.min(resizeX, resizeY);
		Image scaled = current.getScaledInstance((int) (current.getWidth() * resize),
				(int) (current.getHeight() * resize), Image.SCALE_SMOOTH);
		// Writing
		BufferedImage output;
		if (scaled instanceof BufferedImage) {
			output = (BufferedImage) scaled;
		} else {
			output = convertToBufferedImage(scaled);
		}
		result = File.createTempFile(imagePath.getName(), "");
		
		ImageOutputStream out = new FileImageOutputStream(result);
		ImageIO.write(output, "PNG", out);
	}
	
	private final File result;

	private static BufferedImage convertToBufferedImage(Image image) {
		BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return newImage;
	}

	private static BufferedImage rotateCw(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage newImage = new BufferedImage(height, width, img.getType());

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				newImage.setRGB(height - 1 - j, i, img.getRGB(i, j));

		return newImage;
	}
	
	public File getResult() {
		return result;
	}
}
