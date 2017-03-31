package press.gfw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

public class Broadcast {
	
	private static HashMap<String, List<IBroadcastCallback>> dataList = new HashMap<>();
	
	
	public static void register(String action,IBroadcastCallback obj){
		register(Arrays.asList(action), obj);
	}
	

	public static void register(List<String> actions,IBroadcastCallback obj){
		if(null != actions && 0 != actions.size()){
			for(String action : actions){
				if(!dataList.containsKey(action)){
					dataList.put(action, new ArrayList<>());
				}
				List<IBroadcastCallback> list = dataList.get(action);
				list.add(obj);
			}
		}
	}

	public static void unRegister(String action){
		unRegister(action);
	}
	public static void unRegister(String...actions){
		if(null != actions && 0 != actions.length){
			for(String action : actions){
				dataList.remove(action);
			}
		}
	}
	
	public static void sendBroadcast(String action,BroadcastData data){
		sendBroadcast(Arrays.asList(action),data);
	}

	public static void sendBroadcast(List<String> actions,BroadcastData data){
		if(null == actions || 0 == actions.size())
			return ;
		for(String action : actions){
			List<IBroadcastCallback> list = dataList.get(action);
			for(IBroadcastCallback callback : list){
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						callback.recevier(action, data);
					}
				});
			}
		}			
	}
}
