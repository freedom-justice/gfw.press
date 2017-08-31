package press.gfw;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NodeRender extends JPanel implements ListCellRenderer<String> {

	private Map<String, String> ipCity = new HashMap<String, String>();
	public NodeRender(String[] nodes) {
		JLabel ctry = new JLabel();
		JLabel node = new JLabel();
		JLabel ping = new JLabel();
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 3));
		this.add(ctry);
		this.add(node);
		// this.add(ping);
		for (final String ip : nodes) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String country = "未知";
					try {
						country = getNodeCountry(ip);
						if(null == country || "".equals(country.trim()))
							country = "未知";
					} catch (Exception e) {
						e.printStackTrace();
						country = "未知";
					}
					ipCity.put(ip, country);
				}
			}).start();
		}
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus) {
		((JLabel) this.getComponent(0)).setText("["+ipCity.get(value)+"]");
		((JLabel) this.getComponent(1)).setText(value);
		// ((JLabel)this.getComponent(2)).setText("200ms");
		if(isSelected){
			this.setBackground(Color.decode("#B8CFE5"));
		}else{
			this.setBackground(Color.decode("#eeeeee"));
		}
		return this;
	}

	private String getNodeCountry(String ip) throws Exception {
		if(!ip.matches("^[\\d+\\.]+\\d+$")){
			ip = ip.replaceFirst("([\\d+\\.]+\\d+).*", "$1");
		}
		String url = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=" + ip;
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setReadTimeout(30000);
		conn.setConnectTimeout(30000);
		conn.addRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
		conn.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.addRequestProperty("Connection", "keep-alive");
		conn.addRequestProperty("Content-Type", "text/html; charset=UTF-8");
		conn.addRequestProperty("Host", "int.dpool.sina.com.cn");
		conn.addRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		InputStream in = conn.getInputStream();

		String charset = conn.getContentEncoding();
		if (null == charset || "".equals(charset))
			charset = "UTF-8";

		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while (null != (line = reader.readLine())) {
			buffer.append(line);
		}
		in.close();
		reader.close();
		try {
			JSONObject obj = (JSONObject) new JSONParser().parse(buffer.toString());
			return (String) obj.get("country");
		} catch (Exception e) {
			return "未知";
		}
	}

}
