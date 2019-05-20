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
	 * @Title: 构造图片
	 * @Description: 生成水印并返回java.awt.image.BufferedImage
	 * @param file
	 *            源文件(图片)
	 * @param waterFile
	 *            水印文件(图片)
	 * @param x
	 *            距离右下角的X偏移量
	 * @param y
	 *            距离右下角的Y偏移量
	 * @param alpha
	 *            透明度, 选择值从0.0~1.0: 完全透明~完全不透明
	 * @return BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage watermark(File file, File waterFile, int x, int y, float alpha) throws IOException {
		// 获取底图
		BufferedImage buffImg = ImageIO.read(file);
		// 获取层图
		BufferedImage waterImg = ImageIO.read(waterFile);
		// 创建Graphics2D对象，用在底图对象上绘图
		Graphics2D g2d = buffImg.createGraphics();
		int waterImgWidth = waterImg.getWidth();// 获取层图的宽度
		int waterImgHeight = waterImg.getHeight();// 获取层图的高度
		// 在图形和图像中实现混合和透明效果
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
		// 绘制
		g2d.drawImage(waterImg, x, y, waterImgWidth, waterImgHeight, null);
		g2d.dispose();// 释放图形上下文使用的系统资源
		return buffImg;
	}

	/**
	 * 输出水印图片
	 * 
	 * @param buffImg
	 *            图像加水印之后的BufferedImage对象
	 * @param savePath
	 *            图像加水印之后的保存路径
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
	 * 缩放图片方法
	 * 
	 * @param srcImageFile
	 *            要缩放的图片路径
	 * @param result
	 *            缩放后的图片路径
	 * @param height
	 *            目标高度像素
	 * @param width
	 *            目标宽度像素
	 * @param bb
	 *            是否补白
	 */
	public final void scale(String srcImageFile, String result, int height, int width, boolean bb) {
		try {
			double ratio = 0.0; // 缩放比例
			File f = new File(srcImageFile);
			BufferedImage bi = ImageIO.read(f);
			Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);// bi.SCALE_SMOOTH
																				// 选择图像平滑度比缩放速度具有更高优先级的图像缩放算法。
			// 计算比例
			if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
				double ratioHeight = (new Integer(height)).doubleValue() / bi.getHeight();
				double ratioWhidth = (new Integer(width)).doubleValue() / bi.getWidth();
				if (ratioHeight > ratioWhidth) {
					ratio = ratioHeight;
				} else {
					ratio = ratioWhidth;
				}
				AffineTransformOp op = new AffineTransformOp(AffineTransform// 仿射转换
						.getScaleInstance(ratio, ratio), null);// 返回表示剪切变换的变换
				itemp = op.filter(bi, null);// 转换源 BufferedImage 并将结果存储在目标 BufferedImage 中。
			}
			if (bb) {// 补白
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);// 构造一个类型为预定义图像类型之一的
																									// BufferedImage。
				Graphics2D g = image.createGraphics();// 创建一个 Graphics2D，可以将它绘制到此 BufferedImage 中。
				g.setColor(Color.white);// 控制颜色
				g.fillRect(0, 0, width, height);// 使用 Graphics2D 上下文的设置，填充 Shape 的内部区域。
				if (width == itemp.getWidth(null))
					g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null),
							itemp.getHeight(null), Color.white, null);
				else
					g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null),
							itemp.getHeight(null), Color.white, null);
				g.dispose();
				itemp = image;
			}
			ImageIO.write((BufferedImage) itemp, "JPEG", new File(result)); // 输出压缩图片
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
