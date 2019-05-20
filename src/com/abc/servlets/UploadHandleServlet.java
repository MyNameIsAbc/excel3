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
	 * Ĭ���ַ�����
	 */
	private static String encoding = "UTF-8";
	PrintWriter out = null;
	String savePath;
	NewImageUtils newImageUtils = new NewImageUtils();
	// ��Ϣ��ʾ
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
		// �õ��ϴ��ļ��ı���Ŀ¼�����ϴ����ļ������WEB-INFĿ¼�£����������ֱ�ӷ��ʣ���֤�ϴ��ļ��İ�ȫ
		if (savePath == null) {
			savePath = "c:" + File.separator + "upload";
		}
		File file = new File(savePath);
		// �ж��ϴ��ļ��ı���Ŀ¼�Ƿ����
		if (!file.exists() && !file.isDirectory()) {
			System.out.println(savePath + "Ŀ¼�����ڣ���Ҫ����");
			// ����Ŀ¼
			file.mkdir();
		}
		System.out.println("---Ŀ¼:" + savePath);

		String filename = "";
		try {
			// ʹ��Apache�ļ��ϴ���������ļ��ϴ����裺
			// 1������һ��DiskFileItemFactory����
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// 2������һ���ļ��ϴ�������
			ServletFileUpload upload = new ServletFileUpload(factory);
			// ����ϴ��ļ�������������
			upload.setHeaderEncoding("UTF-8");
			// 3���ж��ύ�����������Ƿ����ϴ���������
			if (!ServletFileUpload.isMultipartContent(request)) {
				// ���մ�ͳ��ʽ��ȡ����
				return;
			}
			// 4��ʹ��ServletFileUpload�����������ϴ����ݣ�����������ص���һ��List<FileItem>���ϣ�ÿһ��FileItem��Ӧһ��Form����������
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem item : list) {
				// ���fileitem�з�װ������ͨ�����������
				if (item.isFormField()) {
					String name = item.getFieldName();
					// �����ͨ����������ݵ�������������
					String value = item.getString("UTF-8");
					System.out.println(name + "=" + value);
				} else {// ���fileitem�з�װ�����ϴ��ļ�
						// �õ��ϴ����ļ����ƣ�
					filename = item.getName();
					System.out.println(filename);
					if (filename == null || filename.trim().equals("")) {
						continue;
					}
					// ע�⣺��ͬ��������ύ���ļ����ǲ�һ���ģ���Щ������ύ�������ļ����Ǵ���·���ģ��磺
					// c:\a\b\1.txt������Щֻ�ǵ������ļ������磺1.txt
					// �����ȡ�����ϴ��ļ����ļ�����·�����֣�ֻ�����ļ�������
					filename = filename.substring(filename.lastIndexOf("\\") + 1);
					// ��ȡitem�е��ϴ��ļ���������
					InputStream in = item.getInputStream();
					// ����һ���ļ������
					String outpath = savePath + File.separator + filename + ".jpg";
					FileOutputStream out = new FileOutputStream(outpath);
					// ����һ��������
					byte buffer[] = new byte[1024];
					// �ж��������е������Ƿ��Ѿ�����ı�ʶ
					int len = 0;
					// ѭ�������������뵽���������У�(len=in.read(buffer))>0�ͱ�ʾin���滹������
					while ((len = in.read(buffer)) > 0) {
						// ʹ��FileOutputStream�������������������д�뵽ָ����Ŀ¼(savePath + "\\"
						// + filename)����
						out.write(buffer, 0, len);
					}
					// �ر�������
					in.close();
					// �ر������
					out.close();
					// ɾ�������ļ��ϴ�ʱ���ɵ���ʱ�ļ�
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
							message = "�ļ��ϴ��ɹ���";
							data.put("message", message);
							data.put("QiNiuResponse", t);
							ResponseJsonUtils.json(response, data);
						}
					});
				}
			}
			// ���ñ����ʽ
			response.setContentType("text/plain;charset=" + encoding);
			response.setCharacterEncoding(encoding);

		} catch (Exception e) {
			message = "�ļ��ϴ�ʧ�ܣ�";
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
		// �������Ӳ�
		BufferedImage buffImg = null;
		try {
			buffImg = NewImageUtils.watermark(new File(imagepath), new File(shuiyinfilepath), 0, 0, 1.0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ���ˮӡͼƬ
		String saveFilePath = savePath + File.separator + "new.png";
		newImageUtils.generateWaterFile(buffImg, saveFilePath);
		data.put("number", number);
		data.put("total", total);
		uploadHebingFile(saveFilePath, data, uploadCallback);
	}

	private void uploadscaleFile(String filepath, UploadCallback<String> uploadCallback) {
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss"); // ��ʽ����ǰϵͳ����
		String dateTime = dateFm.format(new java.util.Date());
		String key = dateTime;
		// ����һ����ָ��Zone�����������
		Configuration cfg = new Configuration(Zone.zone1());
		// ...���������ο���ע��
		UploadManager uploadManager = new UploadManager(cfg);
		// ...�����ϴ�ƾ֤��Ȼ��׼���ϴ�
		String accessKey = "SiMKCN168L489zcFWbiCZ46VzYOBngLr26Sx0zFO";
		String secretKey = "9YAlBtoOYd8vBouqrpCZTEoMm1B-5EckdeilGY2n";
		String bucket = "blueplan";
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		// �����Windows����£���ʽ�� D:\\qiniu\\test.png
		// Ĭ�ϲ�ָ��key������£����ļ����ݵ�hashֵ��Ϊ�ļ���
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
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss"); // ��ʽ����ǰϵͳ����
		String dateTime = dateFm.format(new java.util.Date());
		String key = dateTime;
		// ����һ����ָ��Zone�����������
		Configuration cfg = new Configuration(Zone.zone1());
		// ...���������ο���ע��
		UploadManager uploadManager = new UploadManager(cfg);
		// ...�����ϴ�ƾ֤��Ȼ��׼���ϴ�
		String accessKey = "SiMKCN168L489zcFWbiCZ46VzYOBngLr26Sx0zFO";
		String secretKey = "9YAlBtoOYd8vBouqrpCZTEoMm1B-5EckdeilGY2n";
		String bucket = "blueplan";
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		// �����Windows����£���ʽ�� D:\\qiniu\\test.png
		// Ĭ�ϲ�ָ��key������£����ļ����ݵ�hashֵ��Ϊ�ļ���
		try {
			Response response = uploadManager.put(filepath, key + "_watermark", upToken);
			// �����ϴ��ɹ��Ľ��
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
