package joe.warehouse.manager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.soap.SOAPFaultException;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;


public class UploadServlet extends HttpServlet
{
	
	private static final Logger LogService = Logger.getLogger(UploadServlet.class);
	private static String UPLOAD_PATH = null;
	private static String UPLOAD_TEMP_PATH = null;

	private static String TRAINING_URL = null;
	private static String TRAINING_METHOD = null;

	// private static boolean GENERATE_ALL_PHRASE;
	

	public void init(ServletConfig config) throws ServletException
	{ 
		//URL url = this.getClass().getClassLoader().getResource("../../uploadfile");
		
		UPLOAD_PATH = config.getServletContext().getRealPath("/uploadfile");
		if (null != UPLOAD_PATH)
		{
			//UPLOAD_PATH = url.getPath();
			LogService.info("UploadServlet.init() UPLOAD_PATH -> " + UPLOAD_PATH);
//			URL temp_url = this.getClass().getClassLoader().getResource("../../uploadfile/buffer");
//			if (null != temp_url)
//				UPLOAD_TEMP_PATH = temp_url.getPath();
			UPLOAD_TEMP_PATH = config.getServletContext().getRealPath("/uploadfile/buffer");
		}
		else
		{
			LogService.error("UploadServlet.init() UPLOAD_PATH -> NULL");
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType("text/html");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		String responseString ="";
		String returnXmlStr = null;
		String taskStr = request.getParameter("task");
		String webAction = request.getParameter("webAction");
		String ignoreConflict = request.getParameter("ignoreConflict");

		if (webAction == "" || webAction == null)
		{
			webAction = "training";
		}

		if (null == taskStr)
		{
			String sessionid = request.getSession().getId();
			String rName = null;
			try
			{
				File uploadFile = new File(UPLOAD_PATH);
				if (!uploadFile.exists())
				{
					uploadFile.mkdirs();
				}
				File tempPathFile = new File(UPLOAD_TEMP_PATH);
				if (!tempPathFile.exists())
				{
					tempPathFile.mkdirs();
				}
				// Create a factory for disk-based file items
				DiskFileItemFactory factory = new DiskFileItemFactory();

				// Set factory constraints
				factory.setSizeThreshold(4096); // buffer size
				factory.setRepository(tempPathFile); // buffer location
				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);
				// Set overall request size constraint
				upload.setSizeMax(2 * 1024 * 1024);// file size = 1M
				List items = upload.parseRequest(request);
				for (int i = 0; i < items.size(); i++)
				{
					FileItem fi = (FileItem) items.get(i);
					if (null == fi || null == fi.getName())
					{
						continue;
					}

					int index = fi.getName().lastIndexOf("\\");
					if (index < 0)
					{
						index = fi.getName().lastIndexOf("/");
					}

					String fileName = (index == -1) ? fi.getName() : fi.getName().substring(index + 1);
					if (fileName != null)
					{
						int fileSplit = fileName.indexOf(".");
						rName = fileName.substring(0, fileSplit) + "_" + sessionid + fileName.substring(fileSplit);
						File savedFile = new File(UPLOAD_PATH, rName);
						fi.write(savedFile);
					}
				}
				LogService.info("upload succeed rName->" + rName);
			}
			catch (Exception e)
			{
				LogService.error("upload failed->", e);
				rName = "error";
				e.printStackTrace();
			}
			
			out.println(returnMsg("0000","upload success."));

//			out.write("<script language=\"javascript\">parent.callBack(\"" + rName + "\",\"" + webAction + "\",\""
//					+ ignoreConflict + "\");</script>");
		}

		out.flush();
		out.close();
	}
	
	private JSONObject returnMsg(String state ,String msg){
		JSONObject rtnObj = new JSONObject();
		rtnObj.put("state", state);
		rtnObj.put("message", msg);		
		return rtnObj;
	}

	// process call and write training data into web
//	private static Document getRefreshReqDoc(String action, String task)
//	{
//		XmlUtils doc = null;
//		try
//		{
//			doc = new XmlUtils("<request></request>");
//			Element actionEle = doc.addSubNode("action");
//			actionEle.addAttribute("value", action);
//			Element commandEle = doc.addSubNode("command");
//			commandEle.addAttribute("value", task);
//		}
//		catch (Exception e)
//		{
//			LogService.error("Exception in getRefreshReqDoc ->" + e.getMessage());
//		}
//		return doc.getDocument();
//	}
//
//	private static String requestWS(String action, Document doc) throws Exception
//	{
//		String result = null;
//		LogService.info(action + " request to webservice -> " + doc.getRootElement().asXML());
//		try
//		{
//			if (action.equals("training"))
//			{
//				//Object obj = trainingService.request("refresh", doc.getRootElement().asXML());
//				Object obj = ServiceUtils.getWebService().request(doc.getRootElement().asXML());
//				if (obj != null)
//				{
//					result = obj.toString();
//				}
//				else
//				{
//					LogService.info("training webservice is not available!");
//				}
//			}
//		}
//		catch (SOAPFaultException soapExpt)
//		{
//			LogService.error(soapExpt);
//			throw new Exception("WebService throws SOAPFaultException: " + soapExpt.getMessage());
//		}
//		LogService.info(action + " response from webservice -> " + result);
//		return parseResult(result);
//	}
//
//	private static String parseResult(String xmlStr) throws Exception
//	{
//		XmlUtils doc = new XmlUtils();
//		doc.createDocument(xmlStr);
//		String stateStr = doc.getSingleNode("response/state").attributeValue("value");
//		if (null == stateStr)
//		{
//			stateStr = " error";
//		}
//
//		doc = null;
//		return stateStr;
//	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy()
	{
		super.destroy();
		UPLOAD_PATH = null;
		UPLOAD_TEMP_PATH = null;
		TRAINING_URL = null;
		TRAINING_METHOD = null;
	}
}
