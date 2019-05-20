package com.abc.servlets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class NewImageUtils {
	/**
	 * 
	 * @Title: ����ͼƬ
	 * @Description: ����ˮӡ������java.awt.image.BufferedImage
	 * @param file
	 *            Դ�ļ�(ͼƬ)
	 * @param waterFile
	 *            ˮӡ�ļ�(ͼƬ)
	 * @param x
	 *            �������½ǵ�Xƫ����
	 * @param y
	 *            �������½ǵ�Yƫ����
	 * @param alpha
	 *            ͸����, ѡ��ֵ��0.0~1.0: ��ȫ͸��~��ȫ��͸��
	 * @return BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage watermark(File file, File waterFile, int x, int y, float alpha) throws IOException {
		// ��ȡ��ͼ
		BufferedImage buffImg = ImageIO.read(file);
		// ��ȡ��ͼ
		BufferedImage waterImg = ImageIO.read(waterFile);
		// ����Graphics2D�������ڵ�ͼ�����ϻ�ͼ
		Graphics2D g2d = buffImg.createGraphics();
		int waterImgWidth = waterImg.getWidth();// ��ȡ��ͼ�Ŀ��
		int waterImgHeight = waterImg.getHeight();// ��ȡ��ͼ�ĸ߶�
		// ��ͼ�κ�ͼ����ʵ�ֻ�Ϻ�͸��Ч��
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
		// ����
		g2d.drawImage(waterImg, x, y, waterImgWidth, waterImgHeight, null);
		g2d.dispose();// �ͷ�ͼ��������ʹ�õ�ϵͳ��Դ
		return buffImg;
	}

	/**
	 * ���ˮӡͼƬ
	 * 
	 * @param buffImg
	 *            ͼ���ˮӡ֮���BufferedImage����
	 * @param savePath
	 *            ͼ���ˮӡ֮��ı���·��
	 */
	public void generateWaterFile(BufferedImage buffImg, String savePath) {
		int temp = savePath.lastIndexOf(".") + 1;
		try {
			ImageIO.write(buffImg, savePath.substring(temp), new File(savePath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * ����ͼƬ����
	 * 
	 * @param srcImageFile
	 *            Ҫ���ŵ�ͼƬ·��
	 * @param result
	 *            ���ź��ͼƬ·��
	 * @param height
	 *            Ŀ��߶�����
	 * @param width
	 *            Ŀ��������
	 * @param bb
	 *            �Ƿ񲹰�
	 */
	public final void scale(String srcImageFile, String result, int height, int width, boolean bb) {
		try {
			double ratio = 0.0; // ���ű���
			File f = new File(srcImageFile);
			BufferedImage bi = ImageIO.read(f);
			Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);// bi.SCALE_SMOOTH
																				// ѡ��ͼ��ƽ���ȱ������ٶȾ��и������ȼ���ͼ�������㷨��
			// �������
			if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
				double ratioHeight = (new Integer(height)).doubleValue() / bi.getHeight();
				double ratioWhidth = (new Integer(width)).doubleValue() / bi.getWidth();
				if (ratioHeight > ratioWhidth) {
					ratio = ratioHeight;
				} else {
					ratio = ratioWhidth;
				}
				AffineTransformOp op = new AffineTransformOp(AffineTransform// ����ת��
						.getScaleInstance(ratio, ratio), null);// ���ر�ʾ���б任�ı任
				itemp = op.filter(bi, null);// ת��Դ BufferedImage ��������洢��Ŀ�� BufferedImage �С�
			}
			if (bb) {// ����
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);// ����һ������ΪԤ����ͼ������֮һ��
																									// BufferedImage��
				Graphics2D g = image.createGraphics();// ����һ�� Graphics2D�����Խ������Ƶ��� BufferedImage �С�
				g.setColor(Color.white);// ������ɫ
				g.fillRect(0, 0, width, height);// ʹ�� Graphics2D �����ĵ����ã���� Shape ���ڲ�����
				if (width == itemp.getWidth(null))
					g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null),
							itemp.getHeight(null), Color.white, null);
				else
					g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null),
							itemp.getHeight(null), Color.white, null);
				g.dispose();
				itemp = image;
			}
			ImageIO.write((BufferedImage) itemp, "JPEG", new File(result)); // ���ѹ��ͼƬ
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reduceImg(String imgsrc, String imgdist, int widthdist, int heightdist) {
		try {
			File srcfile = new File(imgsrc);
			if (!srcfile.exists()) {
				return;
			}
			Image src = javax.imageio.ImageIO.read(srcfile);

			BufferedImage tag = new BufferedImage((int) widthdist, (int) heightdist, BufferedImage.TYPE_INT_RGB);

			tag.getGraphics().drawImage(src.getScaledInstance(widthdist, heightdist, Image.SCALE_SMOOTH), 0, 0, null);
			// tag.getGraphics().drawImage(src.getScaledInstance(widthdist, heightdist,
			// Image.SCALE_AREA_AVERAGING), 0, 0, null);

			FileOutputStream out = new FileOutputStream(imgdist);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(tag);
			out.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
