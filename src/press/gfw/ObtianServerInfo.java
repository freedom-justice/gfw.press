package press.gfw;

import java.awt.image.DataBufferUShort;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;

/**
 * 用户自动获取gfw.press上的最新节点，端口，密码
 * 
 * @author Administrator
 *
 */
public class ObtianServerInfo extends Thread {

	private String loginName;
	private String loginPwd;
	private Config config = new Config();
	private String uid;
	private String user_pwd;
	private String user_email;
	private UpdateCallback callback;

	public ObtianServerInfo(String loginName, String loginPwd, UpdateCallback callback) {
		super();
		this.loginName = loginName;
		this.loginPwd = loginPwd;
		this.callback = callback;
	}

	public void run() {
		if (null == loginName || "".equals(loginName) || null == loginPwd || "".equals(loginPwd))
			return;
		try {
			JSONObject json = config.getJSON(getHtml("https://gfw.press/user/_login.php",
					"email=" + loginName + "&passwd=" + loginPwd + "&remember_me=week", ""));
			if (!"1".equals(json.get("code"))) {
				config.log("登陆gfw.press失败。" + json.get("msg"));
				javax.swing.JOptionPane.showMessageDialog(null, json.get("msg"), "GFW.Press",
						javax.swing.JOptionPane.ERROR_MESSAGE);
				return;
			}
			config.log("成功获取服务器最新节点信息");
			String html = getHtml("https://gfw.press/user/", null,
					"uid=" + uid + ";user_email=" + user_email + ";user_pwd=" + user_pwd);
			String[] nodes = null;
			String port = "";
			String pwd = "";
			Pattern p = Pattern.compile("<p>([^<]*)</p>");
			Matcher m = p.matcher(html);
			while (m.find()) {
				String group = m.group(1);
				if (group.contains("节点")) {
					group = group.substring(4);
					nodes = group.replaceAll("\\s+", "").split("或");
				}
				if (group.contains("端口")) {
					port = group.substring(4).trim();
				}
				if (group.contains("密码")) {
					pwd = group.substring(4).trim();
					break;
				}
			}
			if (null == nodes || 0 == nodes.length || "".equals(port) || "".equals(pwd)) {
				config.log("解析节点信息失败！");
				javax.swing.JOptionPane.showMessageDialog(null, "解析节点信息失败！", "GFW.Press",
						javax.swing.JOptionPane.ERROR_MESSAGE);
				return;
			}
			saveConfig(nodes, nodes[0], port, pwd);
		} catch (Exception e) {
			config.log(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	private String getHtml(String url, String params, String cookie) throws Exception {
		// 一点点准备工作
		JSONObject json = config.getConfigJSON();
		Object localPort = ((JSONObject) json.get("client")).get("ProxyPort");
		if (null == localPort || "".equals(localPort.toString()))
			localPort = "3128";
		byte[] b = null;
		if (null != params && !"".equals(params)) {
			b = params.getBytes("UTF-8");
		}
		InetSocketAddress addr = new InetSocketAddress("127.0.0.1", Integer.parseInt(localPort.toString()));
		HttpsURLConnection conn = (HttpsURLConnection) new URL(null, url, new sun.net.www.protocol.https.Handler())
				.openConnection(new Proxy(Proxy.Type.HTTP, addr));
		conn.setReadTimeout(30000);
		conn.setConnectTimeout(30000);
		conn.addRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
		conn.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.addRequestProperty("Connection", "keep-alive");
		if (null != b)
			conn.addRequestProperty("Content-Length", b.length + "");
		if (null != b)
			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		else
			conn.addRequestProperty("Content-Type", "text/html; charset=UTF-8");
		if (null != cookie && !"".equals(cookie))
			conn.addRequestProperty("Cookie", cookie);
		conn.addRequestProperty("Host", "gfw.press");
		conn.addRequestProperty("Referer", "https://gfw.press/user/login.php");
		conn.addRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
		conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		if (null != b) {
			OutputStream out = conn.getOutputStream();
			out.write(b);
			out.flush();
			out.close();
		}

		InputStream in = conn.getInputStream();

		String charset = conn.getContentEncoding();
		if (null == charset || "".equals(charset))
			charset = "UTF-8";

		Map<String, List<String>> headers = conn.getHeaderFields();
		for (String key : headers.keySet()) {
			if ("Set-Cookie".equalsIgnoreCase(key)) {
				List<String> vals = headers.get(key);
				for (String val : vals) {
					if (val.contains("user_pwd")) {
						String[] __array = val.split(";");
						user_pwd = __array[0].split("=")[1];
					}
					if (val.contains("user_email")) {
						String[] __array = val.split(";");
						user_email = __array[0].split("=")[1];
					}
					if (val.contains("uid")) {
						String[] __array = val.split(";");
						uid = __array[0].split("=")[1];
					}
				}
			}
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while (null != (line = reader.readLine())) {
			buffer.append(line);
		}
		in.close();
		reader.close();
		return buffer.toString();
	}

	private void saveConfig(String[] serverNode, String server, String port, String password) {
		JSONObject json = config.getConfigJSON();
		((JSONObject) json.get("user")).put("user", loginName);
		((JSONObject) json.get("user")).put("password", loginPwd);
		((JSONObject) json.get("client")).put("ServerPort", port);
		((JSONObject) json.get("client")).put("Password", password);
		((JSONObject) json.get("server")).put("serverNode", Arrays.asList(serverNode));
		config.saveConfigJSON(json);
		if (null != callback) {
			callback.dataUpdate(serverNode, port, password);
		}
	}
}
