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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * GFW.Press配置文件管理
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class Config {

	public static final String CHARSET = "utf-8";

	private File configFile = null;


	public Config() {

		configFile = new File("config.json");
		
	}


	/**
	 * 得到全部配置信息
	 * @return
	 */
	public JSONObject getConfigJSON(){
		return (JSONObject)getJSON(read(configFile));
	}
	
	/**
	 * 得到客户端信息
	 * @return
	 */
	public JSONObject getClientJSON(){
		return (JSONObject)getJSON(read(configFile)).get("client");
	}
	

	/**
	 * 得到服务器信息
	 * @return
	 */
	public JSONObject getServerJSON(){
		return (JSONObject)getJSON(read(configFile)).get("server");
	}
	

	/**
	 * 得到用户信息
	 * @return
	 */
	public JSONObject getUserJSON(){
		return (JSONObject)getJSON(read(configFile)).get("user");
	}
	
	/**
	 * 读取配置文件
	 * @return
	 */
	public void saveConfigJSON(JSONObject json){
		save(configFile, json.toJSONString());
	}
	

	/**
	 * 字符串转JSON对象
	 * 
	 * @param data
	 * @return
	 */
	public JSONObject getJSON(String data) {

		if (data == null || (data = data.trim()).length() == 0) {

			return null;

		}

		JSONParser p = new JSONParser();

		try {

			return (JSONObject) p.parse(data);

		} catch (ParseException ex) {

			log("分析JSON字符串时出错：");

			ex.printStackTrace();

		}

		return null;

	}




	/**
	 * 打印信息
	 * 
	 * @param o
	 */
	public void log(Object o) {

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	/**
	 * 读文件内容
	 * 
	 * @param file
	 * @return
	 */
	public String read(File file) {

		int size = 0;

		if (file == null || !file.exists() || (size = (int) file.length()) == 0) {

			return null;

		}

		byte[] bytes = new byte[size];

		FileInputStream fis = null;

		int count = 0;

		try {

			fis = new FileInputStream(file);

			for (; size != count;) {

				int read = fis.read(bytes, count, size - count);

				if (read == -1) {

					break;

				}

				count += read;

			}

		} catch (IOException ex) {

			log("读文件出错：");

			ex.printStackTrace();

			return null;

		} finally {

			try {

				fis.close();

			} catch (IOException ex) {

			}

		}

		if (count != size) {

			return null;

		}

		try {

			return new String(bytes, CHARSET);

		} catch (UnsupportedEncodingException ex) {

			log("获取文件内容出错：");

			ex.printStackTrace();

		}

		return null;

	}

	/**
	 * 保存内容到文件
	 * 
	 * @param file
	 * @param text
	 * @return
	 */
	public boolean save(File file, String text) {

		if (file == null || text == null || (text = text.trim()).length() == 0) {

			return false;

		}

		FileOutputStream fos = null;

		try {

			fos = new FileOutputStream(file);

			fos.write(text.getBytes(CHARSET));

			fos.flush();

		} catch (IOException ex) {

			log("写文件出错：");

			ex.printStackTrace();

			return false;

		} finally {

			try {

				fos.close();

			} catch (IOException ex) {

			}

		}

		return true;

	}


}