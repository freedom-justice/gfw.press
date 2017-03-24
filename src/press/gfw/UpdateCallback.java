package press.gfw;

/**
 * 成功获取数据更新节点之后的回调
 * @author Administrator
 *
 */
public interface UpdateCallback {

	void dataUpdate(String[] nodes,String port,String password);
	
}
