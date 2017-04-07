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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;

import javax.crypto.SecretKey;

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

/**
 * GFW.Press加密及转发线程
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class EncryptForwardThread extends Thread {

	private static final int BUFFER_SIZE_MIN = 1024 * 8; // 缓冲区最小值，4K

	private static final int BUFFER_SIZE_MAX = 1024 * 128; // 缓冲区最大值，128K

	private static final int BUFFER_SIZE_STEP = 1024 * 4; // 缓冲区自动调整的步长值，4K

	private InputStream inputStream = null;

	private OutputStream outputStream = null;

	private PointThread parent = null;

	private Encrypt aes = null;

	private SecretKey key = null;

	/**
	 * 构造方法
	 * 
	 * @param parent
	 *            父线程
	 * @param inputStream
	 *            输入流
	 * @param outputStream
	 *            输出流
	 * 
	 */
	public EncryptForwardThread(PointThread parent, InputStream inputStream, OutputStream outputStream, SecretKey key) {
		this.parent = parent;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.key = key;
		aes = new Encrypt();

	}

	/**
	 * 打印信息
	 * 
	 * @param o
	 */
	@SuppressWarnings("unused")
	private void log(Object o) {

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	/**
	 * 加密转发
	 */
	public void run() {

		byte[] buffer = new byte[BUFFER_SIZE_MIN];
		byte[] read_bytes = null;
		byte[] encrypt_bytes = null;
		try {
			while (true) {
				int read_num = inputStream.read(buffer);
				if (read_num == -1) {
					break;
				}
				read_bytes = new byte[read_num];
				System.arraycopy(buffer, 0, read_bytes, 0, read_num);
				encrypt_bytes = aes.encryptNet(key, read_bytes);
				if (encrypt_bytes == null) {
					break; // 加密出错，退出
				}
				outputStream.write(encrypt_bytes);

				outputStream.flush();
				// 缓冲区过小，自动增大缓冲区
				if (read_num == buffer.length && read_num < BUFFER_SIZE_MAX) { // 自动调整缓冲区大小
					buffer = null;
					buffer = new byte[read_num + BUFFER_SIZE_STEP];
					// 缓冲区过大，自动增小缓冲区

				} else if (read_num < (buffer.length - BUFFER_SIZE_STEP)
						&& (buffer.length - BUFFER_SIZE_STEP) >= BUFFER_SIZE_MIN) {
					int len = buffer.length - BUFFER_SIZE_STEP;
					buffer = null;
					buffer = new byte[len];
					System.gc();
				}
			}
		} catch (IOException ex) {
			log("加密发送："+ex.getLocalizedMessage());
			Broadcast.sendBroadcast(Windows.BROADCAST_ACTION_WARING, new BroadcastData("msg", "加密发送失败！"));
		}

		buffer = null;
		read_bytes = null;
		encrypt_bytes = null;
		parent.over();

	}

}
