package press.gfw;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class NodeRender extends JLabel implements ListCellRenderer<String> {

	public NodeRender() {
		setIcon(new ImageIcon("img/qrCode.png"));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus) {
		this.setText(value);
		
		return this;
	}
	

}
