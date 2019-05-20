package com.abc.servlets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;

/**
 * Servlet implementation class UploadHandleServlet
 */
@WebServlet("/UploadHandleServlet")
public class UploadHandleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * 默认字符编码
	 */
	private static String encoding = "UTF-8";
	PrintWriter out = null;
	String savePath;
	NewImageUtils newImageUtils = new NewImageUtils();
	// 消息提示
	String message = "";
	int lastnumber = -1;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadHandleServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 得到上传文件的保存目录，将上传的文件存放于WEB-INF目录下，不允许外界直接访问，保证上传文件的安全
		if (savePath == null) {
			savePath = "c:" + File.separator + "upload";
		}
		File file = new File(savePath);
		// 判断上传文件的保存目录是否存在
		if (!file.exists() && !file.isDirectory()) {
			System.out.println(savePath + "目录不存在，需要创建");
			// 创建目录
			file.mkdir();
		}
		System.out.println("---目录:" + savePath);

		String filename = "";
		try {
			// 使用Apache文件上传组件处理文件上传步骤：
			// 1、创建一个DiskFileItemFactory工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// 2、创建一个文件上传解析器
			ServletFileUpload upload = new ServletFileUpload(factory);
			// 解决上传文件名的中文乱码
			upload.setHeaderEncoding("UTF-8");
			// 3、判断提交上来的数据是否是上传表单的数据
			if (!ServletFileUpload.isMultipartContent(request)) {
				// 按照传统方式获取数据
				return;
			}
			// 4、使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem item : list) {
				// 如果fileitem中封装的是普通输入项的数据
				if (item.isFormField()) {
					String name = item.getFieldName();
					// 解决普通输入项的数据的中文乱码问题
					String value = item.getString("UTF-8");
					System.out.println(name + "=" + value);
				} else {// 如果fileitem中封装的是上传文件
						// 得到上传的文件名称，
					filename = item.getName();
					System.out.println(filename);
					if (filename == null || filename.trim().equals("")) {
						continue;
					}
					// 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：
					// c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
					// 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
					filename = filename.substring(filename.lastIndexOf("\\") + 1);
					// 获取item中的上传文件的输入流
					InputStream in = item.getInputStream();
					// 创建一个文件输出流
					String outpath = savePath + File.separator + filename + ".jpg";
					FileOutputStream out = new FileOutputStream(outpath);
					// 创建一个缓冲区
					byte buffer[] = new byte[1024];
					// 判断输入流中的数据是否已经读完的标识
					int len = 0;
					// 循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
					while ((len = in.read(buffer)) > 0) {
						// 使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\"
						// + filename)当中
						out.write(buffer, 0, len);
					}
					// 关闭输入流
					in.close();
					// 关闭输出流
					out.close();
					// 删除处理文件上传时生成的临时文件
					item.delete();

					Map<String, Object> data = new HashMap<String, Object>();
					data.put("date", new Date());
					data.put("filename", filename);
					String scalepath = savePath + File.separator + filename + "_scale.jpg";
					// newImageUtils.scale(outpath, scalepath, 300, 300, true);
					newImageUtils.reduceImg(outpath, scalepath, 800, 800);
					uploadscaleFile(scalepath, new UploadCallback<String>() {

						@Override
						public void onSuccess(String t) {
							// TODO Auto-generated method stub

						}
					});
					getImageandupload(data, scalepath, new UploadCallback<String>() {
						@Override
						public void onSuccess(String t) {
							message = "文件上传成功！";
							data.put("message", message);
							data.put("QiNiuResponse", t);
							ResponseJsonUtils.json(response, data);
						}
					});
				}
			}
			// 设置编码格式
			response.setContentType("text/plain;charset=" + encoding);
			response.setCharacterEncoding(encoding);

		} catch (Exception e) {
			message = "文件上传失败！";
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("message", message);
			data.put("date", new Date());
			data.put("filename", filename);
			data.put("error", e);
			ResponseJsonUtils.json(response, data);
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private void getImageandupload(Map<String, Object> data, String imagepath, UploadCallback<String> uploadCallback) {
		File[] children = new File(savePath).listFiles();
		int total = 0;
		for (int i = 0; i < children.length; i++) {
			String filename = children[i].getName();
			if (filename.endsWith(".png")&&filename.startsWith("0")) {
				total++;
			}
		}
		int number = (new Random().nextInt(total)) + 1;

		String shuiyinfilepath;
		if (number < 10) {
			shuiyinfilepath = savePath + File.separator + "00" + number + ".png";
		} else {
			shuiyinfilepath = savePath + File.separator + "0" + number + ".png";
		}
		while (lastnumber == number) {
			number = (new Random().nextInt(51)) + 1;
		}
		System.out.println("number:" + number);
		lastnumber = number;
		// 构建叠加层
		BufferedImage buffImg = null;
		try {
			buffImg = NewImageUtils.watermark(new File(imagepath), new File(shuiyinfilepath), 0, 0, 1.0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 输出水印图片
		String saveFilePath = savePath + File.separator + "new.png";
		newImageUtils.generateWaterFile(buffImg, saveFilePath);
		data.put("number", number);
		data.put("total", total);
		uploadHebingFile(saveFilePath, data, uploadCallback);
	}

	private void uploadscaleFile(String filepath, UploadCallback<String> uploadCallback) {
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss"); // 格式化当前系统日期
		String dateTime = dateFm.format(new java.util.Date());
		String key = dateTime;
		// 构造一个带指定Zone对象的配置类
		Configuration cfg = new Configuration(Zone.zone1());
		// ...其他参数参考类注释
		UploadManager uploadManager = new UploadManager(cfg);
		// ...生成上传凭证，然后准备上传
		String accessKey = "SiMKCN168L489zcFWbiCZ46VzYOBngLr26Sx0zFO";
		String secretKey = "9YAlBtoOYd8vBouqrpCZTEoMm1B-5EckdeilGY2n";
		String bucket = "blueplan";
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		// 如果是Windows情况下，格式是 D:\\qiniu\\test.png
		// 默认不指定key的情况下，以文件内容的hash值作为文件名
		try {
			Response response = uploadManager.put(filepath, key + "_scale", upToken);
			uploadCallback.onSuccess(response.bodyString());
		} catch (QiniuException ex) {
			try {
				uploadCallback.onSuccess(ex.response.bodyString());
			} catch (QiniuException e) {
				uploadCallback.onSuccess(e.toString());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void uploadHebingFile(String filepath, Map<String, Object> data, UploadCallback<String> uploadCallback) {
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss"); // 格式化当前系统日期
		String dateTime = dateFm.format(new java.util.Date());
		String key = dateTime;
		// 构造一个带指定Zone对象的配置类
		Configuration cfg = new Configuration(Zone.zone1());
		// ...其他参数参考类注释
		UploadManager uploadManager = new UploadManager(cfg);
		// ...生成上传凭证，然后准备上传
		String accessKey = "SiMKCN168L489zcFWbiCZ46VzYOBngLr26Sx0zFO";
		String secretKey = "9YAlBtoOYd8vBouqrpCZTEoMm1B-5EckdeilGY2n";
		String bucket = "blueplan";
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		// 如果是Windows情况下，格式是 D:\\qiniu\\test.png
		// 默认不指定key的情况下，以文件内容的hash值作为文件名
		try {
			Response response = uploadManager.put(filepath, key + "_watermark", upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			System.out.println(putRet.key);
			System.out.println(putRet.hash);
			System.out.println("upload response:" + response.bodyString());

			JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
			if (jsonObject.containsKey("error")) {
				String errormessage = jsonObject.getString("error");
				if (errormessage.replaceAll(" ", "").equalsIgnoreCase("fileexists")) {

				}
			}
			String fileName = putRet.key;
			String domainOfBucket = "http://p31vckjd8.bkt.clouddn.com";
			String finalUrl = String.format("%s/%s", domainOfBucket, fileName);
			data.put("finalurl", finalUrl);
			System.out.println(finalUrl);
			uploadCallback.onSuccess(response.bodyString());
		} catch (QiniuException ex) {
			try {
				uploadCallback.onSuccess(ex.response.bodyString());
			} catch (QiniuException e) {
				uploadCallback.onSuccess(e.toString());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
