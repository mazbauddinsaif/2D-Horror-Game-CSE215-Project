package game.data;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {
    private static final String MUSIC_FOLDER = "D:\\North South University\\2nd Semester (10)\\CSE215\\Lab 253\\Project\\2D-Horror-Game-CSE215-Project\\Escape From The Ghost\\Music\\";
    private final String COIN_SOUND_PATH = MUSIC_FOLDER + "Coin.wav";
    private final String KEY_SOUND_PATH = MUSIC_FOLDER + "key.wav";
    private final String CAUGHT_SOUND_PATH = MUSIC_FOLDER + "caught.wav";
    private final String LOCKED_SOUND_PATH = MUSIC_FOLDER + "locked_door.wav";
    private final String BACKGROUND_MUSIC_PATH = MUSIC_FOLDER + "TestAudio.wav";
    private final String BUTTON_CLICK_SOUND_PATH = MUSIC_FOLDER + "buttonClick.wav";

    private Clip coinClip, keyClip, caughtClip, lockedClip, bgmClip, buttonClickClip;

    public AudioPlayer() {
        loadAudioClips();
    }

    /**
     * Loads all required audio files into memory clips.
     */
    public void loadAudioClips() {
        coinClip = loadClip(COIN_SOUND_PATH);
        keyClip = loadClip(KEY_SOUND_PATH);
        caughtClip = loadClip(CAUGHT_SOUND_PATH);
        lockedClip = loadClip(LOCKED_SOUND_PATH);
        bgmClip = loadClip(BACKGROUND_MUSIC_PATH);
        buttonClickClip = loadClip(BUTTON_CLICK_SOUND_PATH);
    }

    /**
     * Utility method to load a single audio file.
     */
    private Clip loadClip(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("Audio file not found: " + filePath);
                return null;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading audio clip " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Plays a single sound clip once.
     * @param type The type of sound to play.
     */
    public void playSound(SoundType type) {
        Clip clip = getClipByType(type);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    /**
     * Starts the background music loop.
     */
    public void startBGM() {
        if (bgmClip != null) {
            bgmClip.setFramePosition(0);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Stops the background music.
     */
    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    /**
     * Maps the enum type to the corresponding clip.
     */
    private Clip getClipByType(SoundType type) {
        return switch (type) {
            case COIN -> coinClip;
            case KEY -> keyClip;
            case CAUGHT -> caughtClip;
            case LOCKED -> lockedClip;
            case BUTTON_CLICK -> buttonClickClip;
            case BGM -> bgmClip; // Although BGM should be handled by startBGM/stopBGM
        };
    }

    /**
     * Enum for all available sound types.
     */
    public enum SoundType {
        COIN, KEY, CAUGHT, LOCKED, BUTTON_CLICK, BGM
    }
}