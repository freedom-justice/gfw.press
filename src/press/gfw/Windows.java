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
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
				Broadcast.unRegister(Windows.BROADCAST_ACTION_ERROR,Windows.BROADCAST_ACTION_NORMAL,Windows.BROADCAST_ACTION_WARING);
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
				serverHostField.setSelectedIndex(0);
				serverPortField.setText(serverPort);
				passwordField.setText(password);
				proxyPortField.setText(proxyPort);
				break;
			}
		}
	}

	/**
	 * 系统托盘
	 */
	private class TrayListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			toFront();
			setVisible(true);
		}
	}

	/**
	 * 主窗口
	 */
	private class WindowsListener implements WindowListener {

		@Override
		public void windowActivated(WindowEvent e) {

		}

		@Override
		public void windowClosed(WindowEvent e) {

		}

		@Override
		public void windowClosing(WindowEvent e) {

			setVisible(false);

		}

		@Override
		public void windowDeactivated(WindowEvent e) {

		}

		@Override
		public void windowDeiconified(WindowEvent e) {

		}

		@Override
		public void windowIconified(WindowEvent e) {

		}

		@Override
		public void windowOpened(WindowEvent e) {

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

	private JTextField serverPortField = new JTextField(), proxyPortField = new JTextField(), loginNameField = new JTextField();

	private JPasswordField passwordField = new JPasswordField(), loginPwdField = new JPasswordField();

	public static final String BROADCAST_ACTION_ERROR = "gfw.press.error";
	public static final String BROADCAST_ACTION_NORMAL = "gfw.press.normal";
	public static final String BROADCAST_ACTION_WARING = "gfw.press.waring";
	
	public Windows() {

		super("GFW.Press");

		config = new Config();

		initTray("logo.png","");

		initWindows();

		initForm();

		initButton();

		initBorder();
		
		Broadcast.register(Arrays.asList(Windows.BROADCAST_ACTION_ERROR,Windows.BROADCAST_ACTION_NORMAL,Windows.BROADCAST_ACTION_WARING), this);

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

		// 服务面板
		JPanel serverPanel = new JPanel();

		GridLayout serverLayout = new GridLayout(6, 2, 0, 5);

		serverPanel.setLayout(serverLayout);
		// 帐号信息
		serverPanel.add(new JLabel("登录帐号："));

		serverPanel.add(loginNameField);

		serverPanel.add(new JLabel("登录密码："));

		serverPanel.add(loginPwdField);
		// 服务信息

		serverPanel.add(new JLabel("节点地址："));

		serverPanel.add(serverHostField);

		serverPanel.add(new JLabel("节点端口："));

		serverPanel.add(serverPortField);

		serverPanel.add(new JLabel("连接密码："));

		serverPanel.add(passwordField);

		serverPanel.add(new JLabel("本地端口："));

		serverPanel.add(proxyPortField);

		loadConfig();

		add(serverPanel, BorderLayout.CENTER);

	}

	private void initTray(String iconName,String tip) {
		logo = Toolkit.getDefaultToolkit().getImage(iconName);
		setIconImage(logo);
		icon = new TrayIcon(logo, tip);
		icon.setImageAutoSize(true);
		icon.addActionListener(new TrayListener());
		tray = SystemTray.getSystemTray();
		try {
			TrayIcon[] icons = tray.getTrayIcons();
			for(TrayIcon i : icons)
				tray.remove(i);
			tray.add(icon);
		} catch (AWTException ex) {
			log("添加系统托盘图标出错：");
			ex.printStackTrace();
		}
	}
	
	private void initWindows() {

		Dimension dimemsion = Toolkit.getDefaultToolkit().getScreenSize();
			
		setSize(480, 300);

		setLocation((int) (dimemsion.getWidth() - getWidth()) / 2, (int) (dimemsion.getHeight() - getHeight()) / 2);

		// setAlwaysOnTop(true);

		setResizable(false);

		addWindowListener(new WindowsListener());

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

		for (int i = 0; i < nodes.length; i++) {
			serverHosts[i] = nodes[i] + "";
		}

		serverHostField.setModel(new DefaultComboBoxModel<Object>(nodes));

		serverHostField.setSelectedItem(serverHost);

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
				if (!port.equals(serverPort)) {
					serverPort = port;
					isUpdate = true;
				}
				if (!pwd.equals(password)) {
					password = pwd;
					isUpdate = true;
				}
				for (int i = 0; i < nodes.length; i++) {
					if (!nodes[i].equals(serverHosts[i])) {
						isUpdate = true;
						break;
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

	private String state = null;
	@Override
	public void recevier(String action, BroadcastData data) {
		if(Windows.BROADCAST_ACTION_ERROR.equals(action) && !Windows.BROADCAST_ACTION_ERROR.equals(state)){
			initTray("logo_error.png",data.getData("msg")+"");
		}else if(Windows.BROADCAST_ACTION_NORMAL.equals(action) && !Windows.BROADCAST_ACTION_NORMAL.equals(state)){
			initTray("logo.png",data.getData("msg")+"");
		}else if(Windows.BROADCAST_ACTION_WARING.equals(action)  && !Windows.BROADCAST_ACTION_WARING.equals(state)){
			initTray("logo_waring.png",data.getData("msg")+"");
		}
		state = action;
	}

}
