package audioplaylist;

import java.awt.*;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

/** Example of playing all audio files in a given directory. */
public class AudioPlaylist extends Application {
    private String filePath;
  final Label currentlyPlaying = new Label();
  final Label totalPlaying = new Label();
  final ProgressBar progress = new ProgressBar();
  final Button open = new Button("open");
  MediaView mediaView = new MediaView();
  private ChangeListener<Duration> progressChangeListener;

  public static void main(String[] args) throws Exception { launch(args); }

  public void start(final Stage stage) throws Exception {
    stage.setTitle("Simple Audio Player");
    final StackPane layout = new StackPane();
    final Button skip = new Button("Skip");
    final Button play = new Button("Pause");
    final Button stop = new Button("Stop");
    final Button back = new Button("Back");
  Button invisiblePause = new Button("Pause");
   List<MediaPlayer> players = new ArrayList<MediaPlayer>();
    invisiblePause.setVisible(false);
    stop.setDisable(true);
    back.setDisable(true);
    play.setDisable(true);
    skip.setDisable(true);
    play.prefHeightProperty().bind(invisiblePause.heightProperty());
    play.prefWidthProperty().bind(invisiblePause.widthProperty());
  
    
    // layout the scene.
    open.setStyle("-fx-font-size:16;");
    stop.setStyle("-fx-font-size:16;");
    back.setStyle("-fx-font-size:16;");
    layout.setStyle("-fx-background-color: cornsilk; -fx-font-size: 18; -fx-padding: 10; -fx-alignment: center;");
    currentlyPlaying.setStyle(" -fx-font-size: 18;  -fx-alignment: center;");
  // open.setPreferredSize(new Dimension(20, 20));
    layout.getChildren().addAll(
      invisiblePause,
      VBoxBuilder.create().spacing(10).children(
       HBoxBuilder.create().spacing(10).alignment(Pos.BASELINE_LEFT).children(open,stop,back,currentlyPlaying,totalPlaying).build(),
        HBoxBuilder.create().spacing(10).alignment(Pos.CENTER).children(skip, play, progress, mediaView).build()
      ).build()
    );
    
    progress.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(progress, Priority.ALWAYS);
    Scene scene = new Scene(layout, 600, 110);
    stage.setScene(scene);
    stage.show();
   
    open.setOnAction(new EventHandler<ActionEvent>() {
         @Override public void handle(ActionEvent actionEvent) {
            
    FileChooser filechooser = new FileChooser();
       // FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a File (*.mp3)", "*.mp3");
      // filechooser.getExtensionFilters().add(filter);
       File filee=filechooser.showOpenDialog(null);
     //  path=new URL(fil., filePath);
      // filePath=filee.toURI().toString();
    // determine the source directory for the playlist (either the first parameter to the program or a 
    final List<String> params = getParameters().getRaw();
    final File dir = (params.size() > 0)
      ? new File(params.get(0))
      //: new File("C:\\Users\\Public\\Music\\Sample Music");
        //C:\\Users\\Peter\\Documents\\NetBeansProjects\\AudioPlaylist\\src\\Music
          //   : new File("C:\\Users\\Peter\\Documents\\NetBeansProjects\\AudioPlaylist\\src\\Music");
            : new File(filee.getParent());
    if (!dir.exists() && dir.isDirectory()) {
       currentlyPlaying.setText("Cannot find audio source directory: " + dir);
    }

    // create some media players.
    
    for (String file : dir.list(new FilenameFilter() {
        
      @Override public boolean accept(File dir, String name) {
        return name.endsWith(".mp3");
      }
    })) players.add(createPlayer("file:///" + (dir + "\\" + file).replace("\\", "/").replaceAll(" ", "%20")));
    if (players.isEmpty()) {
      currentlyPlaying.setText("No audio found in " + dir);
      return;
      
    }
    else {
        totalPlaying.setTextFill(Color.web("#0076a3"));
        totalPlaying.setText("Total: "+String.valueOf(players.size()));}
    // create a view to show the mediaplayers.
    mediaView = new MediaView(players.get(0));
    

    // play each audio file in turn.
    for (int i = 0; i < players.size(); i++) {
      final MediaPlayer player     = players.get(i);
      final MediaPlayer nextPlayer = players.get((i + 1) % players.size());
      player.setOnEndOfMedia(new Runnable() {
        @Override public void run() {
          player.currentTimeProperty().removeListener(progressChangeListener);
          mediaView.setMediaPlayer(nextPlayer);
         // nextPlayer.play();
          mediaView.getMediaPlayer().pause();
          play.setText("Play");
        }
      });
    }
    
    // allow the user to skip a track.
    skip.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        MediaPlayer nextPlayer = players.get((players.indexOf(curPlayer) + 1) % players.size());
        mediaView.setMediaPlayer(nextPlayer);
        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        nextPlayer.play();
        back.setDisable(false);
      }
    });

    // allow the user to play or pause a track.
    play.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        if ("Pause".equals(play.getText())) {
          mediaView.getMediaPlayer().pause();
          play.setText("Play");
        } else {
          mediaView.getMediaPlayer().play();
          play.setText("Pause");
        }
      }
    });
     
    // display the name of the currently playing track.
    mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
      @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
        setCurrentlyPlaying(newPlayer);
      }
    });
    // stop.setVisible(true);
     open.setDisable(true);
     stop.setDisable(false);
     play.setDisable(false);
     skip.setDisable(false);
     back.setDisable(false);
     //stage.requestFocus();
     stop.setFocusTraversable(false);
      skip.setFocusTraversable(false);
    // start playing the first track.
    mediaView.setMediaPlayer(players.get(0));
    mediaView.getMediaPlayer().play();
    setCurrentlyPlaying(mediaView.getMediaPlayer());

         }  // silly invisible button used as a template to get the actual preferred size of the Pause button.
   });
    //allow to usr stop playing and request for open file 
    stop.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        
          mediaView.getMediaPlayer().stop();
          currentlyPlaying.setText("");
        //  mediaView.setMediaPlayer(null);
        //  open.setVisible(true);
          players.clear();
          currentlyPlaying.setText("please choose File to play");
          currentlyPlaying.setTextFill(Color.web("#0076a3"));
          totalPlaying.setText("");
          stop.setDisable(true);
         // play.setEnabled(false);
         play.setDisable(true);
         skip.setDisable(true);
         back.setDisable(true);
         open.setDisable(false);
          
          
          
           
      
      }
    });
   
    // allow the user to backward a track.
    back.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
         final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        if(players.indexOf(curPlayer)==0){ 
             back.setDisable(true);
        }
        else {
        MediaPlayer backPlayer = players.get((players.indexOf(curPlayer) - 1) % players.size());
        mediaView.setMediaPlayer(backPlayer);
        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        backPlayer.play();
         back.setDisable(false);
        }}
    });
  
     stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
    if (event.getCode().equals(KeyCode.ENTER)) {
       final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        MediaPlayer nextPlayer = players.get((players.indexOf(curPlayer) + 1) % players.size());
        mediaView.setMediaPlayer(nextPlayer);
        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        nextPlayer.play();
       back.setDisable(false);
       }
    if (event.getCode().equals(KeyCode.SPACE)){
         mediaView.getMediaPlayer().pause();    }
    if (event.getCode().equals(KeyCode.BACK_SPACE)) {
        final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        if(players.indexOf(curPlayer)==0){ 
             back.setDisable(true);
        }
        else {
        MediaPlayer backPlayer = players.get((players.indexOf(curPlayer) - 1) % players.size());
        mediaView.setMediaPlayer(backPlayer);
        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        backPlayer.play();
         back.setDisable(false);
        }
    }
});
  }

  /** sets the currently playing label to the label of the new media player and updates the progress monitor. */
  private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
    progress.setProgress(0);
    progressChangeListener = new ChangeListener<Duration>() {
      @Override public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
        progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
      }
    };
    newPlayer.currentTimeProperty().addListener(progressChangeListener);

    String source = newPlayer.getMedia().getSource();
    source = source.substring(0, source.length() - ".mp3".length());
    source = source.substring(source.lastIndexOf("/") + 1).replaceAll("%20", " ");
    currentlyPlaying.setTextFill(Color.web("#000000"));
    currentlyPlaying.setText("Now Playing: " + source);
  }

  /** @return a MediaPlayer for the given source which will report any errors it encounters */
  private MediaPlayer createPlayer(String aMediaSrc) {
    final MediaPlayer player = new MediaPlayer(new Media(aMediaSrc));
    player.setOnError(new Runnable() {
      @Override public void run() {
         currentlyPlaying.setText("Media error occurred: " + player.getError());
      }
    });
    return player;
  }
}