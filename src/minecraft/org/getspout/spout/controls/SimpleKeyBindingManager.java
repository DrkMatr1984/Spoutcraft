package org.getspout.spout.controls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.getspout.spout.client.SpoutClient;
import org.getspout.spout.io.FileUtil;
import org.getspout.spout.packet.PacketKeyBinding;
import org.spoutcraft.spoutcraftapi.keyboard.KeyBindingManager;
import org.spoutcraft.spoutcraftapi.keyboard.KeyBinding;
import org.yaml.snakeyaml.Yaml;

public class SimpleKeyBindingManager implements KeyBindingManager {
	private HashMap<Integer, KeyBinding> bindingsForKey = new HashMap<Integer, KeyBinding>();
	private ArrayList<KeyBinding> bindings;
	
	public SimpleKeyBindingManager(){
		load();
	}
	
	public void registerControl(KeyBinding binding){
		KeyBinding result = null;
		for(KeyBinding check:bindings){
			if(check.getId().equals(binding.getId()) && check.getPlugin().equals(binding.getPlugin())){
				result = check;
			}
		}
		if(result != null){
			result.takeChanges(binding);
		} else {
			bindings.add(binding);
		}
		updateBindings();
		save();
	}
	
	public void unregisterControl(KeyBinding binding){
		bindings.remove(binding);
		bindingsForKey.remove(binding.getKey());
		save();
	}
	
	private void updateBindings() {
		if(bindings == null){
			bindings = new ArrayList<KeyBinding>();
			return;
		}
		
		bindingsForKey.clear();
		for(KeyBinding binding:bindings){
			bindingsForKey.put(binding.getKey(), binding);
		}
	}
	
	@Override
	public void save(){
		Yaml yaml = new Yaml();
		File file = getDataFile();
		try {
			FileWriter writer = new FileWriter(file);
			yaml.dump(bindings, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private File getDataFile() {
		return new File(FileUtil.getSpoutcraftDirectory(), "bindings.yml");
	}

	@Override
	public void load(){
		Yaml yaml = new Yaml();
		try {
			bindings = yaml.loadAs(new FileReader(getDataFile()), ArrayList.class);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateBindings();
	}

	@Override
	public void pressKey(int key, boolean keyReleased, int screen) {
		
		KeyBinding binding = bindingsForKey.get(key);
		if(binding==null){
			return;
		}
		SpoutClient.getInstance().getPacketManager().sendSpoutPacket(new PacketKeyBinding(binding, key, keyReleased, screen));
	}
}
