package press.gfw;

import java.util.HashMap;

public interface IBroadcastCallback {
	
	public void recevier(String action,BroadcastData data);
	
}

class BroadcastData{
	private HashMap<String, Object> data = new HashMap<>();
	
	public BroadcastData(String key,Object val){
		data.put(key, val);
	}
	
	public BroadcastData putData(String key,Object val){
		if(!data.containsKey(key))
			data.put(key, val);
		return this;
	}
	public Object getData(String key){
		return data.get(key);
	}
}