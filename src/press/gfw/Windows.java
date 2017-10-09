/**
* 
*    GFW.Press
*    Copyright (C) 2016  chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*    
**/
package press.gfw;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * 
 * GFW.Press客户端图形界面
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class Windows extends JFrame implements IBroadcastCallback {

	/**
	 * 退出
	 */
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			if (command == null) {
				return;
			}

			switch (command) {
			case "退出":
				setVisible(false);
				if (tray != null && icon != null) {
					tray.remove(icon);
				}
				Broadcast.unRegister(Windows.BROADCAST_ACTION_ERROR, Windows.BROADCAST_ACTION_NORMAL,
						Windows.BROADCAST_ACTION_WARING, Windows.BROADCAST_ACTION_QRCODE);
				System.exit(0);
				break;

			case "确定":
				setVisible(false);
				boolean edit = false;
				if (!serverHost.equals(serverHostField.getSelectedItem().toString().trim())) {
					serverHost = serverHostField.getSelectedItem().toString().trim();
					edit = true;
				}
				if (!serverPort.equals(serverPortField.getText().trim())) {
					serverPort = serverPortField.getText().trim();
					edit = true;
				}
				String _password = new String(passwordField.getPassword()).trim();
				if (!password.equals(_password)) {
					password = _password;
					edit = true;
				}
				if (!proxyPort.equals(proxyPortField.getText().trim())) {
					proxyPort = proxyPortField.getText().trim();
					edit = true;
				}
				if (!loginName.equals(loginNameField.getText().trim())) {
					loginName = loginNameField.getText().trim();
					edit = true;
				}
				String loginPwdString = new String(loginPwdField.getPassword()).trim();
				if (!loginPwd.equals(loginPwdString)) {
					loginPwd = loginPwdString;
					edit = true;
				}
				if (edit) {
					saveConfig();
				}
				start();
				break;
			case "取消":
				setVisible(false);
				serverHostField.setSelectedItem(serverHost);
				serverPortField.setText(serverPort);
				passwordField.setText(password);
				proxyPortField.setText(proxyPort);
				break;
			}
		}
	}

	private static final long serialVersionUID = -7964262019916663094L;

	public static void main(String[] args) throws IOException {

		Windows windows = new Windows();

		windows.start();

	}

	private Client client = null;

	private SystemTray tray = null;

	private TrayIcon icon = null;

	private JButton exitButton = null;

	private JButton okButton = null;

	private JButton cancelButton = null;

	private Image logo = null;

	private Config config = null;

	private String serverHost = "", serverPort = "", password = "", proxyPort = "", loginName = "", loginPwd = "";

	private String[] serverHosts = null;

	private JComboBox serverHostField = new JComboBox();

	private JTextField serverPortField = new JTextField(), proxyPortField = new JTextField(),
			loginNameField = new JTextField();

	private JPasswordField passwordField = new JPasswordField(), loginPwdField = new JPasswordField();

	private JLabel qrCoder = new JLabel();

	public static final String BROADCAST_ACTION_ERROR = "gfw.press.error";
	public static final String BROADCAST_ACTION_NORMAL = "gfw.press.normal";
	public static final String BROADCAST_ACTION_WARING = "gfw.press.waring";
	public static final String BROADCAST_ACTION_QRCODE = "gfw.press.qrcode";

	public Windows() {

		super("GFW.Press");

		config = new Config();

		initTray("img/logo.png", "");

		initWindows();

		initForm();

		initButton();

		initBorder();

		Broadcast.register(Arrays.asList(Windows.BROADCAST_ACTION_ERROR, Windows.BROADCAST_ACTION_NORMAL,
				Windows.BROADCAST_ACTION_WARING, Windows.BROADCAST_ACTION_QRCODE), this);

		if (password.length() < 8) {

			setVisible(true);

		}

	}

	private void initBorder() {

		// 备用
		add(new JLabel(), BorderLayout.EAST);

		add(new JLabel(), BorderLayout.NORTH);

		add(new JLabel(), BorderLayout.WEST);

	}

	private void initButton() {

		ButtonListener buttonActionListener = new ButtonListener();

		// 按钮
		JPanel buttonPanel = new JPanel();

		buttonPanel.setPreferredSize(new Dimension(getWidth(), 60));

		exitButton = new JButton("退出");

		exitButton.addActionListener(buttonActionListener);

		okButton = new JButton("确定");

		okButton.addActionListener(buttonActionListener);

		cancelButton = new JButton("取消");

		cancelButton.addActionListener(buttonActionListener);

		buttonPanel.add(exitButton);

		buttonPanel.add(new JLabel("     "));

		buttonPanel.add(okButton);

		buttonPanel.add(new JLabel("     "));

		buttonPanel.add(cancelButton);

		add(buttonPanel, BorderLayout.SOUTH);

	}

	private void initForm() {

		// 主面板
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		// 服务面板
		JPanel serverPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		serverPanel.setLayout(layout);
		GridBagConstraints s = new GridBagConstraints();// 定义一个GridBagConstraints，
		// 是用来控制添加进的组件的显示位置
		s.fill = GridBagConstraints.BOTH;
		// 帐号信息
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 3;
		JLabel lab = null;
		serverPanel.add(lab = new JLabel("登录帐号："));
		lab.setPreferredSize(new Dimension(80, 26));
		layout.setConstraints(lab, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(loginNameField);
		layout.setConstraints(loginNameField, s);
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(lab = new JLabel("登录密码："));
		lab.setPreferredSize(new Dimension(80, 26));
		layout.setConstraints(lab, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(loginPwdField);
		layout.setConstraints(loginPwdField, s);
		// 服务信息
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(lab = new JLabel("节点地址："));
		lab.setPreferredSize(new Dimension(80, 26));
		layout.setConstraints(lab, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(serverHostField);
		layout.setConstraints(serverHostField, s);
		serverHostField.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				qrCoder.setIcon(null);
				qrCoder.setIcon(new ImageIcon("img/loading.gif"));
				new Thread(new Runnable() {
					public void run() {
						try {
							int width = 100;
							int height = 100;
							String serverNode = (String) serverHostField.getSelectedItem();
							if (null == serverNode || "".equals(serverNode))
								return;
							if (!serverNode.matches("^[\\d+\\.]+\\d+$")) {
								serverNode = serverNode.replaceFirst("([\\d+\\.]+\\d+).*", "$1");
							}
							String base64 = encodeBase64(
									("aes-256-cfb:" + password + "@" + serverNode + ":" + serverPort).getBytes());
							base64 = "gp://" + base64 + "?remarks=gfw.press";
							Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
							hints.put(EncodeHintType.CHARACTER_SET, "GBK");
							BitMatrix matrix = null;
							try {
								matrix = new MultiFormatWriter().encode(base64, BarcodeFormat.QR_CODE, width, height,
										hints);
							} catch (WriterException ee) {
								ee.printStackTrace();
							}
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							int _width = matrix.getWidth();
							int _height = matrix.getHeight();
							BufferedImage image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
							for (int x = 0; x < width; x++) {
								for (int y = 0; y < height; y++) {
									image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
								}
							}
							ImageIO.write(image, "png", out);
							BroadcastData data = new BroadcastData("qrCode", out.toByteArray());
							data.putData("result", "success");
							Broadcast.sendBroadcast(Windows.BROADCAST_ACTION_QRCODE, data);
							out.close();
						} catch (Exception ee) {
							ee.printStackTrace();
							BroadcastData data = new BroadcastData("result", "fail");
							Broadcast.sendBroadcast(Windows.BROADCAST_ACTION_QRCODE, data);
						}
					}
				}).start();

			}
		});
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(lab = new JLabel("节点端口："));
		lab.setPreferredSize(new Dimension(80, 26));
		layout.setConstraints(lab, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(serverPortField);
		layout.setConstraints(serverPortField, s);
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(lab = new JLabel("连接密码："));
		lab.setPreferredSize(new Dimension(80, 26));
		layout.setConstraints(lab, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(passwordField);
		layout.setConstraints(passwordField, s);
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(lab = new JLabel("本地端口："));
		lab.setPreferredSize(new Dimension(80, 26));
		layout.setConstraints(lab, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 3;
		serverPanel.add(proxyPortField);
		layout.setConstraints(proxyPortField, s);

		mainPanel.add(serverPanel);
		mainPanel.add(qrCoder);

		qrCoder.setHorizontalTextPosition(SwingConstants.CENTER);
		qrCoder.setHorizontalAlignment(SwingConstants.CENTER);
		qrCoder.setVerticalAlignment(SwingConstants.CENTER);
		qrCoder.setVerticalTextPosition(SwingConstants.BOTTOM);

		loadConfig();

		add(mainPanel, BorderLayout.CENTER);

	}

	private void initTray(String iconName, String tip) {
		logo = Toolkit.getDefaultToolkit().getImage(iconName);
		setIconImage(logo);
		icon = new TrayIcon(logo, tip);
		icon.setImageAutoSize(true);
		icon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toFront();
				setVisible(true);
			}
		});
		tray = SystemTray.getSystemTray();
		try {
			TrayIcon[] icons = tray.getTrayIcons();
			for (TrayIcon i : icons)
				tray.remove(i);
			tray.add(icon);
		} catch (AWTException ex) {
			log("添加系统托盘图标出错：");
			ex.printStackTrace();
		}
	}

	private void initWindows() {

		Dimension dimemsion = Toolkit.getDefaultToolkit().getScreenSize();

		setSize(470, 310);

		setLocation((int) (dimemsion.getWidth() - getWidth()) / 2, (int) (dimemsion.getHeight() - getHeight()) / 2);

		setAlwaysOnTop(true);

		setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});

		// 主窗口布局
		BorderLayout windowsLayout = new BorderLayout(20, 20);

		setLayout(windowsLayout);

	}

	private void loadConfig() {

		JSONObject client = config.getClientJSON();
		JSONObject user = config.getUserJSON();
		JSONObject server = config.getServerJSON();

		if (client != null) {

			serverHost = client.get("ServerHost") == null ? "" : (String) client.get("ServerHost");

			serverPort = client.get("ServerPort") == null ? "" : (String) client.get("ServerPort");

			password = client.get("Password") == null ? "" : (String) client.get("Password");

			proxyPort = client.get("ProxyPort") == null ? "" : (String) client.get("ProxyPort");

		}

		if (user != null) {
			loginName = user.get("user") == null ? "" : (String) user.get("user");
			loginPwd = user.get("password") == null ? "" : (String) user.get("password");
		}

		// 登录帐号信息

		loginNameField.setText(loginName);

		loginPwdField.setText(loginPwd);

		// 服务信息
		JSONArray array = (JSONArray) server.get("serverNode");

		serverHostField.removeAllItems();

		Object[] nodes = array.toArray();

		serverHosts = new String[nodes.length];
		int oldNodeIndex = 0;
		for (int i = 0; i < nodes.length; i++) {
			serverHosts[i] = nodes[i] + "";
			if (serverHosts[i].startsWith(serverHost))
				oldNodeIndex = i;
		}
		serverHostField.setModel(new DefaultComboBoxModel<Object>(nodes));
		serverHostField.setRenderer(new NodeRender(serverHosts));
		serverHostField.setSelectedIndex(oldNodeIndex);
		serverHostField.updateUI();

		serverPortField.setText(serverPort);

		passwordField.setText(password);

		proxyPortField.setText(proxyPort);

	}

	/**
	 * 打印信息
	 * 
	 * @param o
	 */
	private void log(Object o) {

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	@SuppressWarnings("unchecked")
	private void saveConfig() {

		JSONObject json = config.getConfigJSON();

		((JSONObject) json.get("user")).put("user", loginName);

		((JSONObject) json.get("user")).put("password", loginPwd);

		((JSONObject) json.get("client")).put("ServerHost", serverHost);

		((JSONObject) json.get("client")).put("ServerPort", serverPort);

		((JSONObject) json.get("client")).put("Password", password);

		((JSONObject) json.get("client")).put("ProxyPort", proxyPort);

		config.saveConfigJSON(json);

	}

	public void start() {

		new ObtianServerInfo(loginName, loginPwd, new UpdateCallback() {
			@Override
			public void dataUpdate(String[] nodes, String port, String pwd) {
				boolean isUpdate = false;
				boolean isNewNode = false;
				if (!port.equals(serverPort)) {
					serverPort = port;
					isUpdate = true;
				}
				if (!pwd.equals(password)) {
					password = pwd;
					isUpdate = true;
				}
				if (nodes.length != serverHosts.length) {
					isUpdate = true;
				} else {
					for (int i = 0; i < nodes.length; i++) {
						if (!nodes[i].equals(serverHost)) 
							isNewNode = true;
						else{
							isNewNode = false;
							break;
						}
					}
					if(isNewNode){
						serverHost = serverHosts[0];
					}
					for (int i = 0; i < nodes.length; i++) {
						if (!nodes[i].equals(serverHosts[i])) {
							isUpdate = true;
							break;
						}
					}
				}
				if (isUpdate) {
					start();
					loadConfig();
				}
			}
		}).start();

		if (client != null && !client.isKill()) {
			if (serverHost.equals(client.getServerHost()) && serverPort.equals(String.valueOf(client.getServerPort()))
					&& password.equals(client.getPassword())
					&& proxyPort.equals(String.valueOf(client.getListenPort()))) {
				return;
			} else {
				client.kill();
			}
		}

		client = new Client(serverHost, serverPort, password, proxyPort);
		client.start();
		// log(client.getName());
	}

	/***
	 * encode by Base64
	 */
	public static String encodeBase64(byte[] input) throws Exception {
		Class clazz = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod = clazz.getMethod("encode", byte[].class);
		mainMethod.setAccessible(true);
		Object retObj = mainMethod.invoke(null, new Object[] { input });
		return (String) retObj;
	}

	private String state = null;

	@Override
	public void recevier(String action, BroadcastData data) {
		if (Windows.BROADCAST_ACTION_ERROR.equals(action) && !Windows.BROADCAST_ACTION_ERROR.equals(state)) {
			initTray("img/logo_error.png", data.getData("msg") + "");
		} else if (Windows.BROADCAST_ACTION_NORMAL.equals(action) && !Windows.BROADCAST_ACTION_NORMAL.equals(state)) {
			initTray("img/logo.png", data.getData("msg") + "");
		} else if (Windows.BROADCAST_ACTION_WARING.equals(action) && !Windows.BROADCAST_ACTION_WARING.equals(state)) {
			initTray("img/logo_waring.png", data.getData("msg") + "");
		}
		if (Windows.BROADCAST_ACTION_QRCODE.equals(action)) {
			if ("success".equalsIgnoreCase(data.getData("result") + "")) {
				byte[] bytes = (byte[]) data.getData("qrCode");
				qrCoder.setIcon(new ImageIcon(bytes));
				qrCoder.setText("请使用小火箭扫描");
			} else {
				qrCoder.setIcon(new ImageIcon("img/rupture.png"));
				qrCoder.setText("二维码生成失败");
			}
			qrCoder.updateUI();
			return;
		}
		state = action;
	}

}
