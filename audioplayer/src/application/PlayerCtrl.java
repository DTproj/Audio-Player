package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import de.umass.lastfm.Album;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Track;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;


public class PlayerCtrl {
	
	private MediaPlayer mp;
	
	private ArrayList<MediaPlayer> mpl = new ArrayList<MediaPlayer>();
	
	private int nxt;
	private int prev;
	
	@FXML 
	private Slider volume;
	
	@FXML
	private ProgressBar trackProg;
	
	private ChangeListener<Duration> pcl;
	
	//playlist init
	
	@FXML
	private ListView<String> playlist;
	
	private ObservableList<String> pL;
	
	@FXML
	private CheckBox shuff;
	
	@FXML
	private CheckBox rep;
	
	//meta
	
	@FXML private Text artist;
	@FXML private Text aUrl;
	@FXML private Text aPc;
	@FXML private Text aLst;
	
	@FXML private Text album;
	@FXML private Text rDate;
	
	@FXML private Text track;
	@FXML private Text dur;
	
	//file
	
	@FXML
	private void openM() {
		
		FileChooser fC = new FileChooser();
		fC.getExtensionFilters().add(new ExtensionFilter("Audio Files", "*.wav", "*.mp3"));
		
		List<File> f = fC.showOpenMultipleDialog(null);
		
		ArrayList<String> paths = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		
		
		
		if(f == null) {
			return;
		}
		
		else {
			
			for(File file : f) {
				
				String path = file.getAbsolutePath();
				String path2 = path.replace(" ", "%20");
				String path3 = path2.replace("[", "%5B");
				String path4 = path3.replace("]", "%5D");
				String pathAb = path4.replace("\\", "/");
				
				paths.add(pathAb);
				
				names.add(file.getName());
				
			}
			
			if(mpl != null) {
				
				mpl.clear();
				
				listAR.clear();
				listALB.clear();
				listTR.clear();
				
				artist.setText("Skip track if info doesn't load, or use the search feature.");
				aUrl.setText("");
				aPc.setText("");
				aLst.setText("");
				album.setText("");
				rDate.setText("");
				track.setText("");
				dur.setText("");
			}
			
			
			
			for(String p : paths) {
				
				Media m = new Media("file:/" + p);
				
				mpl.add(new MediaPlayer(m));
				
				
			}
			
			if(mp != null) {
				
				mp.stop();
				mp.currentTimeProperty().removeListener(pcl);
				
			}
			
			if(shuff.isSelected()) {
				Collections.shuffle(mpl);
			}
			
			pL = FXCollections.observableArrayList(names);
			
			playlist.setItems(pL);
			
			loadMtd();
			
			loadKey();
			
			playM(0);
			
		}
		
		
	}
	
	//play
	
	private void playM(int i) {
		
		if(i >= mpl.size()) {
			return;
		}
		
		else {
		
			mp = mpl.get(i);
			
			mp.play();
			
			//progress
			
			trackProg.setProgress(0);

		    pcl = new ChangeListener<Duration>() {
		      public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
		        trackProg.setProgress(1.0 * mp.getCurrentTime().toMillis() / mp.getTotalDuration().toMillis());
		      }
		    };
		    
		    mp.currentTimeProperty().addListener(pcl);
			
			//volume
		
			volume.setValue(mp.getVolume() * 100);
			volume.valueProperty().addListener(new InvalidationListener() {

				@Override
				public void invalidated(Observable arg0) {
				
					mp.setVolume(volume.getValue() / 100);
				
				}
			});
			
			//load meta
			
			if(listAR.isEmpty() == false) {
				aInfo(i);
			}
			else {
				artist.setText("Skip track if info doesn't load, or use the search feature.");
			}
			
			
			//controls
			
			i++;
			
			
			if(i == mpl.size() && rep.isSelected()) {
				i = 0;
			}
			
			final int z = i;
			
			prev = z - 2;
			nxt = z;
		
			mp.setOnEndOfMedia(new Runnable() {

				@Override
				public void run() {
					
					mp.stop();
					mp.currentTimeProperty().removeListener(pcl);
					playM(z);
				
				}
			});
		
		}
		
	}
	
	//button
	
	@FXML
	private void pzBtn() {
		
		if(mp == null) {
			return;
		}
		
		mp.pause();
	}
	
	@FXML
	private void playBtn() {
		
		if(mp == null) {
			return;
		}
		
		mp.play();
	}
	
	@FXML
	private void stopBtn() {
		
		if(mp == null) {
			return;
		}
		
		mp.stop();
	}
	
	//next
	
	@FXML
	private void nxtBtn() {
		
		if(mp == null) {
			return;
		}
		
		else if(nxt >= mpl.size()) {
			return;
		}
		
		else {
			
			mp.stop();
			
			mp.currentTimeProperty().removeListener(pcl);
			
			playM(nxt);
		}
		
	}
	
	//prev
	
	@FXML
	private void prevBtn() {
		
		if(mp == null) {
			return;
		}
		
		else if(prev < 0) {
			return;
		}
		
		else {
			
			mp.stop();
			
			mp.currentTimeProperty().removeListener(pcl);
			
			playM(prev);
		}
		
	}
	
	//meta workaround
	
	private ArrayList<String> listAR = new ArrayList<String>();
	private ArrayList<String> listALB = new ArrayList<String>();
	private ArrayList<String> listTR = new ArrayList<String>();
	
	private void loadMtd() {
		
		for(MediaPlayer medpl : mpl) {
			
			medpl.getMedia().getMetadata().addListener(new MapChangeListener<String, Object> (){

				@Override
				public void onChanged(Change<? extends String, ? extends Object> arg0) {
							
					if(arg0.wasAdded()) {
						if(arg0.getKey().equals("artist")) {
							listAR.add(arg0.getValueAdded().toString());
						}
						else if(arg0.getKey().equals("album")) {
							listALB.add(arg0.getValueAdded().toString());
						}
						else if(arg0.getKey().equals("title")) {
							listTR.add(arg0.getValueAdded().toString());
						}
					}
							
				}
						
			});	
		}
	}
	
	
	
	//lastfm
	
	//load key
	
	public String key;
	
	public void loadKey() {
		
		String keyLoc = "Properties\\key.txt";
		
		try {
			
			FileReader fr = new FileReader(keyLoc);
			BufferedReader bfr = new BufferedReader(fr);
			
			key = bfr.readLine();
			
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	//oldkey
	
	@FXML private  TextField keyTf;
	
	
	@FXML
	public void keyReq() {
		
		if(keyTf.getText().equals(null)) {
			return;
		}
			
		key = keyTf.getText();
	}
	*/
	
	public void aInfo(int i) {
		
		//key check
		
		if(key == null || key.length() < 32) {
			artist.setText("Missing key or key too short.");
			
			aUrl.setText("");
			aPc.setText("");
			aLst.setText("");
			album.setText("");
			rDate.setText("");
			track.setText("");
			dur.setText("");
			
			return;
		}
		else {
			if(Artist.getInfo(listAR.get(i), key) == null && key.length() == 32) {
				
				artist.setText("Incorrect key.");
				
				aUrl.setText("");
				aPc.setText("");
				aLst.setText("");
				album.setText("");
				rDate.setText("");
				track.setText("");
				dur.setText("");
				
				return;
			}
		}
		
		//aInfo
		
		if(Artist.getInfo(listAR.get(i), key).getName() == null) {
			
			artist.setText("Artist not found, try the search feature.");
			
			aUrl.setText("");
			aPc.setText("");
			aLst.setText("");
			album.setText("");
			rDate.setText("");
			track.setText("");
			dur.setText("");
			
			return;
		}
		
		int ii = Artist.getInfo(listAR.get(i), key).getPlaycount();
		int iii = Artist.getInfo(listAR.get(i), key).getListeners();
		int d = Track.getInfo(listAR.get(i), listTR.get(i), key).getDuration();
		Date dd = Album.getInfo(listAR.get(i), listALB.get(i), key).getReleaseDate();
	
		artist.setText(Artist.getInfo(listAR.get(i), key).getName());
		aUrl.setText(Artist.getInfo(listAR.get(i), key).getUrl());
		aPc.setText(String.valueOf(ii));
		aLst.setText(String.valueOf(iii));
		
		
		album.setText(Album.getInfo(listAR.get(i), listALB.get(i), key).getName());
		rDate.setText(String.valueOf(dd));
	
		track.setText(Track.getInfo(listAR.get(i), listTR.get(i), key).getName());
		dur.setText(String.valueOf(d) + " seconds");
		
		
	}
	
	//search
	
	@FXML private TextField sArt;
	@FXML private TextField sAlb;
	@FXML private TextField sTr;
	
	@FXML
	public void searchArt() {
		
		//key check
		
		loadKey();
		
		if(key == null || key.length() < 32) {
			artist.setText("Missing key or key too short.");
			
			aUrl.setText("");
			aPc.setText("");
			aLst.setText("");
			album.setText("");
			rDate.setText("");
			track.setText("");
			dur.setText("");
			
			return;
		}
		else {
				if(Artist.getInfo(sArt.getText(), key) == null && key.length() == 32) {
					artist.setText("Incorrect key.");
					
					aUrl.setText("");
					aPc.setText("");
					aLst.setText("");
					album.setText("");
					rDate.setText("");
					track.setText("");
					dur.setText("");
					
					return;
					
				}
		}
		
		//searchArt
		
		int ii = Artist.getInfo(sArt.getText(), key).getPlaycount();
		int iii = Artist.getInfo(sArt.getText(), key).getListeners();
		
		artist.setText(Artist.getInfo(sArt.getText(), key).getName());
		aUrl.setText(Artist.getInfo(sArt.getText(), key).getUrl());
		aPc.setText(String.valueOf(ii));
		aLst.setText(String.valueOf(iii));
		
	}
	
	@FXML
	public void searchAlb() {
		
		String arts;
		
		if(sArt.getText().isEmpty()) {
			arts = artist.getText();
		}
		else {
			arts = sArt.getText(); 
		}
		
		//key check
		
		if(key == null || key.length() < 32) {
			artist.setText("Missing key or key too short.");
			
			aUrl.setText("");
			aPc.setText("");
			aLst.setText("");
			album.setText("");
			rDate.setText("");
			track.setText("");
			dur.setText("");
			
			return;
		}
		else {
			if(Artist.getInfo(sArt.getText(), key) == null && key.length() == 32) {
				
				artist.setText("Incorrect key.");
				
				aUrl.setText("");
				aPc.setText("");
				aLst.setText("");
				album.setText("");
				rDate.setText("");
				track.setText("");
				dur.setText("");
				
				return;
			}
		}
		
		//searchAlb
		
		Date dd = Album.getInfo(arts, sAlb.getText(), key).getReleaseDate();
		
		album.setText(Album.getInfo(arts, sAlb.getText(), key).getName());
		rDate.setText(String.valueOf(dd));
	}
	
	@FXML
	public void searchTr() {
		
		
		String arts;
		
		if(sArt.getText().isEmpty()) {
			arts = artist.getText();
		}
		else {
			arts = sArt.getText(); 
		}
		
		//key check
		
		if(key == null || key.length() < 32) {
			artist.setText("Missing key or key too short.");
					
			aUrl.setText("");
			aPc.setText("");
			aLst.setText("");
			album.setText("");
			rDate.setText("");
			track.setText("");
			dur.setText("");
					
			return;
		}
		else {
			if(Artist.getInfo(sArt.getText(), key) == null && key.length() == 32) {
				
				artist.setText("Incorrect key.");
				
				aUrl.setText("");
				aPc.setText("");
				aLst.setText("");
				album.setText("");
				rDate.setText("");
				track.setText("");
				dur.setText("");
				
				return;
			}
		}
		
		//searchTr
		
		int d = Track.getInfo(arts, sTr.getText(), key).getDuration();
		
		track.setText(Track.getInfo(arts, sTr.getText(), key).getName());
		dur.setText(String.valueOf(d) + " seconds");
	}
	
	
	//old
	 
	/*
	 
	@FXML
	private void open(){
		
		FileChooser fC = new FileChooser();
		fC.getExtensionFilters().add(new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"));
		
		File f = fC.showOpenDialog(null);
		
		if(f == null) {
			return;
		}
		else {
			
			String path = f.getAbsolutePath();
			String path2 = path.replace(" ", "%20");
			String path3 = path2.replace("[", "%5B");
			String path4 = path3.replace("]", "%5D");
			String pathAb = path4.replace("\\", "/");
			
			if(mp != null) {
			
				mp.stop();
			}
		
			play(pathAb);
		}
	}
	
	private void play(String path) {
		
		Media m = new Media("file:/" + path);
		mp = new MediaPlayer(m);
		mp.play();
		
		volume.setValue(mp.getVolume() * 100);
		volume.valueProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable arg0) {
				
				mp.setVolume(volume.getValue() / 100);
				
			}
			
		});
	}
	*/

}
