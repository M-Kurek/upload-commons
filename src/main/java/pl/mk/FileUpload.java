package pl.mk;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet("/uploadFile")
public class FileUpload extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String UPLOAD_DIRECTORY = "upload";

	private static final int ONE_MB = 1024 * 1024;
	private static final int MEMORY_THRESHOLD 	= 128 * ONE_MB;
	private static final int MAX_FILE_SIZE 		= 2000 * ONE_MB;
	private static final int MAX_REQUEST_SIZE	= 2000 * ONE_MB;

	String uploadPath;

	DiskFileItemFactory factory = new DiskFileItemFactory();
	private ServletFileUpload upload;

	ExecutorService executorService = Executors.newFixedThreadPool(2);

	private static final Logger LOG = Logger.getLogger("servletlog");

	@Override
	public void init() throws ServletException {

		uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
		setLimits();
		initProgressIndicator();
		createTargetFolder();

	}

	private void setLimits() {

		factory.setSizeThreshold(MEMORY_THRESHOLD);
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

		upload = new ServletFileUpload(factory);
		upload.setFileSizeMax(MAX_FILE_SIZE);
		upload.setSizeMax(MAX_REQUEST_SIZE);

	}

	private void initProgressIndicator() {

		ProgressListener progressListener = new ProgressListener(){
			public void update(long pBytesRead, long pContentLength, int pItems) {
				if (pContentLength == -1) {
					LOG.info("So far, " + pBytesRead + " bytes have been read.");
				} else {
					LOG.info("" + (int)(100* pBytesRead/pContentLength) + "%, " + pBytesRead + " of " + pContentLength
							+ " uploaded.");
				}
			}
		};
		upload.setProgressListener(progressListener);
		
	}

	private void createTargetFolder() {
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
			LOG.info("new upload folder : " + uploadDir.getAbsolutePath());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (!ServletFileUpload.isMultipartContent(request)) {
			// not interested
			PrintWriter writer = response.getWriter();
			writer.println("Expected enctype 'multipart/form-data'");
			writer.flush();
			return;
		}
		String fileName = "unknown";
		try {
			fileName = processRequest(request);
		} catch(FileNotFoundException e) {
			LOG.log(Level.SEVERE, "Server problems with target folder :" + fileName, e);
			request.setAttribute("message",
					"Cannot write file - contact support.");
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Problem uploading :" + fileName, ex);
			request.setAttribute("message",
					"There was an error: " + ex.getMessage());
		}

		request.getSession().setAttribute("uploadedFile", fileName);
		response.sendRedirect("upload.jsp");

	}

	private String processRequest(HttpServletRequest request)
			throws FileUploadException, Exception {

		List<FileItem> formItems = upload.parseRequest(request);

		String fileName = "unknown";
		if (formItems != null && formItems.size() > 0) {
			for (FileItem item : formItems) {
				if (!item.isFormField()) {
					fileName = processUploadedFile(item);
					request.setAttribute("message",
							"File '" + fileName + "' uploaded and stored");
				} else {
					processFormField(item);
				}
			}
		}
		return fileName;
	}

	private void processFormField(FileItem item) {
		// TODO NIY
		LOG.warning("upload not severd yet : " + item.getName() + " / " + item.getContentType());
	}

	private String processUploadedFile(FileItem item) throws Exception {

		String fileName = new File(item.getName()).getName();
		String filePath = uploadPath + File.separator + fileName;

		if ((new File(filePath)).exists()) {
			LOG.warning("will override " + filePath);
		}

		File storeFile = new File(filePath);
		try {
			item.write(storeFile);
			LOG.info("uploaded to : " + storeFile);
		} catch(Exception e) {
			LOG.log(Level.SEVERE, 
					"upload crash for file '" + storeFile.getAbsolutePath() + "'", e);
		}
		return fileName;
	}
}