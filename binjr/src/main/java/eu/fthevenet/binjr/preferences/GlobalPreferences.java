package eu.fthevenet.binjr.preferences;

import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Manifest;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Stores the global user preferences for the application.
 *
 * @author Frederic Thevenet
 */
public class GlobalPreferences {
    private static final Logger logger = LogManager.getLogger(GlobalPreferences.class);
    public static final String HTTP_WWW_BINJR_EU = "http://www.binjr.eu";
    public static final String HTTP_LATEST_RELEASE = "https://github.com/fthevenet/binjr/releases/latest";
    private static final String CHART_ANIMATION_ENABLED = "chartAnimationEnabled";
    private static final String DOWN_SAMPLING_THRESHOLD = "downSamplingThreshold";
    private static final String SAMPLE_SYMBOLS_VISIBLE = "sampleSymbolsVisible";
    private static final String DOWN_SAMPLING_ENABLED = "downSamplingEnabled";
    private static final String BINJR_GLOBAL = "binjr/global";
    private static final String USE_SOURCE_COLORS = "useSourceColors";
    private static final String MOST_RECENT_SAVE_FOLDER = "mostRecentSaveFolder";
    private static final String MOST_RECENT_SAVED_WORKSPACE = "mostRecentSavedWorkspace";
    private static final String LOAD_LAST_WORKSPACE_ON_STARTUP = "loadLastWorkspaceOnStartup";
    private static final String RECENT_FILES = "recentFiles";
    public static final int MAX_RECENT_FILES = 20;
    private final Manifest manifest;
    private BooleanProperty loadLastWorkspaceOnStartup;
    private BooleanProperty downSamplingEnabled;
    private IntegerProperty downSamplingThreshold;
    private BooleanProperty sampleSymbolsVisible;
    private BooleanProperty chartAnimationEnabled;
    private Preferences prefs;
    private BooleanProperty useSourceColors;
    private StringProperty mostRecentSaveFolder;
    private Property<Path> mostRecentSavedWorkspace;
    private Deque<String> recentFiles;

    private static class GlobalPreferencesHolder {
        private final static GlobalPreferences instance = new GlobalPreferences();
    }

    private GlobalPreferences() {
        prefs = Preferences.userRoot().node(BINJR_GLOBAL);
        mostRecentSaveFolder = new SimpleStringProperty(prefs.get(MOST_RECENT_SAVE_FOLDER, System.getProperty("user.home")));
        mostRecentSaveFolder.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                prefs.put(MOST_RECENT_SAVE_FOLDER, newValue);
            }
        });
        downSamplingEnabled = new SimpleBooleanProperty(prefs.getBoolean(DOWN_SAMPLING_ENABLED, true));
        downSamplingEnabled.addListener((observable, oldValue, newValue) -> prefs.putBoolean(DOWN_SAMPLING_ENABLED, newValue));
        downSamplingThreshold = new SimpleIntegerProperty(prefs.getInt(DOWN_SAMPLING_THRESHOLD, 5000));
        downSamplingThreshold.addListener((observable, oldValue, newValue) -> prefs.putInt(DOWN_SAMPLING_THRESHOLD, newValue.intValue()));
        sampleSymbolsVisible = new SimpleBooleanProperty(prefs.getBoolean(SAMPLE_SYMBOLS_VISIBLE, false));
        sampleSymbolsVisible.addListener((observable, oldValue, newValue) -> prefs.putBoolean(SAMPLE_SYMBOLS_VISIBLE, newValue));
        chartAnimationEnabled = new SimpleBooleanProperty(prefs.getBoolean(CHART_ANIMATION_ENABLED, false));
        chartAnimationEnabled.addListener((observable, oldValue, newValue) -> prefs.putBoolean(CHART_ANIMATION_ENABLED, newValue));
        useSourceColors = new SimpleBooleanProperty(prefs.getBoolean(USE_SOURCE_COLORS, true));
        useSourceColors.addListener((observable, oldValue, newValue) -> prefs.putBoolean(USE_SOURCE_COLORS, newValue));
        mostRecentSavedWorkspace = new SimpleObjectProperty<>(Paths.get(prefs.get(MOST_RECENT_SAVED_WORKSPACE, "Untitled")));
        mostRecentSavedWorkspace.addListener((observable, oldValue, newValue) -> prefs.put(MOST_RECENT_SAVED_WORKSPACE, newValue.toString()));
        loadLastWorkspaceOnStartup = new SimpleBooleanProperty(prefs.getBoolean(LOAD_LAST_WORKSPACE_ON_STARTUP, true));
        loadLastWorkspaceOnStartup.addListener((observable, oldValue, newValue) -> prefs.putBoolean(LOAD_LAST_WORKSPACE_ON_STARTUP, newValue));
        String recentFileString = prefs.get(RECENT_FILES, "");
        recentFiles = new ArrayDeque<>(Arrays.stream(recentFileString.split("\\|")).filter(s -> s != null && s.trim().length() > 0).collect(Collectors.toList()));
        this.manifest = getManifest();
        if (logger.isDebugEnabled()) {
            logger.debug("Global preferences initial values");
            logger.debug("  downSamplingThreshold = " + downSamplingThreshold.getValue());
            logger.debug("  sampleSymbolsVisible = " + sampleSymbolsVisible.getValue());
            logger.debug("  downSamplingEnabled = " + downSamplingEnabled.getValue());
            logger.debug("  mostRecentSaveFolder = " + mostRecentSaveFolder.getValue());
            logger.debug("  useSourceColors = " + useSourceColors.getValue());
            logger.debug("  sampleSymbolsVisible = " + sampleSymbolsVisible.getValue());
            logger.debug("  mostRecentSavedWorkspace = " + mostRecentSavedWorkspace.getValue());
            logger.debug("  loadLastWorkspaceOnStartup = " + loadLastWorkspaceOnStartup.getValue());
            logger.debug("  recentFileString = " + recentFileString);
        }
    }

    /**
     * Returns the singleton instance of {@link GlobalPreferences}
     *
     * @return the singleton instance of {@link GlobalPreferences}
     */
    public static GlobalPreferences getInstance() {
        return GlobalPreferencesHolder.instance;
    }

    /**
     * Returns true if the chart animation is enabled, false otherwise.
     *
     * @return true if the chart animation is enabled, false otherwise.
     */
    public Boolean getChartAnimationEnabled() {
        return downSamplingEnabled.getValue();
    }

    /**
     * Returns the chart animation property
     *
     * @return the chart animation property
     */
    public BooleanProperty chartAnimationEnabledProperty() {
        return chartAnimationEnabled;
    }

    /**
     * Enables or disables the chart animation
     *
     * @param chartAnimationEnabled true to enable the chart animation, false otherwise.
     */
    public void setChartAnimationEnabled(boolean chartAnimationEnabled) {
        this.chartAnimationEnabled.setValue(chartAnimationEnabled);
    }

    /**
     * Returns true if the chart symbols are visible, false otherwise.
     *
     * @return true if the chart symbols are visible, false otherwise.
     */
    public Boolean getSampleSymbolsVisible() {
        return sampleSymbolsVisible.getValue();
    }

    /**
     * Return the chart symbols visibility property
     *
     * @return the chart symbols visibility property
     */
    public BooleanProperty sampleSymbolsVisibleProperty() {
        return sampleSymbolsVisible;
    }

    /**
     * Sets the visibility of chart symbols
     *
     * @param sampleSymbolsVisible the visibility of chart symbols
     */
    public void setSampleSymbolsVisible(boolean sampleSymbolsVisible) {
        this.sampleSymbolsVisible.setValue(sampleSymbolsVisible);
    }

    /**
     * Returns true if series down-sampling is enabled, false otherwise.
     *
     * @return true if series down-sampling is enabled, false otherwise.
     */
    public Boolean getDownSamplingEnabled() {
        return downSamplingEnabled.getValue();
    }

    /**
     * Returns the down-sampling property
     *
     * @return the down-sampling property
     */
    public BooleanProperty downSamplingEnabledProperty() {
        return downSamplingEnabled;
    }

    /**
     * Enables or disables series down-sampling
     *
     * @param downSamplingEnabled true to enable series down-sampling, false otherwise.
     */
    public void setDownSamplingEnabled(boolean downSamplingEnabled) {
        this.downSamplingEnabled.setValue(downSamplingEnabled);
    }

    /**
     * Returns the series down-sampling threshold value
     *
     * @return the series down-sampling threshold value
     */
    public int getDownSamplingThreshold() {
        return downSamplingThreshold.getValue();
    }

    /**
     * Returns the property for the series down-sampling threshold value
     *
     * @return the property for the series down-sampling threshold value
     */
    public IntegerProperty downSamplingThresholdProperty() {
        return downSamplingThreshold;
    }

    /**
     * Sets the series down-sampling threshold value
     *
     * @param downSamplingThreshold the series down-sampling threshold value
     */
    public void setDownSamplingThreshold(int downSamplingThreshold) {
        this.downSamplingThreshold.setValue(downSamplingThreshold);
    }

    /**
     * Returns true if the series graph should use the colors defined in the source, false otherwise
     *
     * @return true if the series graph should use the colors defined in the source, false otherwise
     */
    public Boolean isUseSourceColors() {
        return useSourceColors.getValue();
    }

    /**
     * The useSourceColors property
     *
     * @return the useSourceColors property
     */
    public BooleanProperty useSourceColorsProperty() {
        return useSourceColors;
    }

    /**
     * Set to true to have the series graph use the colors defined in the source, false otherwise
     *
     * @param useSourceColors Set to true to have the series graph use the colors defined in the source, false otherwise
     */
    public void setUseSourceColors(Boolean useSourceColors) {
        this.useSourceColors.setValue(useSourceColors);
    }

    /**
     * Gets the path of the folder of the most recently saved item
     *
     * @return the path of the folder of the most recently saved item
     */
    public String getMostRecentSaveFolder() {
        return mostRecentSaveFolder.getValue();
    }

    /**
     * The mostRecentSaveFolder property
     *
     * @return the mostRecentSaveFolder property
     */
    public StringProperty mostRecentSaveFolderProperty() {
        return mostRecentSaveFolder;
    }

    /**
     * Sets the path of the folder of the most recently saved item
     *
     * @param mostRecentSaveFolder the path of the folder of the most recently saved item
     */
    public void setMostRecentSaveFolder(String mostRecentSaveFolder) {
        if (mostRecentSaveFolder == null) {
            throw new IllegalArgumentException("mostRecentSaveFolder parameter cannot be null");
        }
        this.mostRecentSaveFolder.setValue(mostRecentSaveFolder);
    }

    /**
     * Gets the path from the most recently saved workspace
     *
     * @return the path from the most recently saved workspace
     */
    public Path getMostRecentSavedWorkspace() {
        return mostRecentSavedWorkspace.getValue();
    }

    /**
     * The mostRecentSavedWorkspace property
     *
     * @return the  mostRecentSavedWorkspace property
     */
    public Property<Path> mostRecentSavedWorkspaceProperty() {
        return mostRecentSavedWorkspace;
    }

    /**
     * Sets  the path from the most recently saved workspace
     *
     * @param mostRecentSavedWorkspace the path from the most recently saved workspace
     */
    public void setMostRecentSavedWorkspace(Path mostRecentSavedWorkspace) {
        if (mostRecentSavedWorkspace == null) {
            throw new IllegalArgumentException("mostRecentSavedWorkspace parameter cannot be null");
        }
        this.mostRecentSavedWorkspace.setValue(mostRecentSavedWorkspace);
    }

    /**
     * Returns true if  the most recently saved workspace should be re-opned on startup, false otherwise
     *
     * @return true if  the most recently saved workspace should be re-opned on startup, false otherwise
     */
    public boolean isLoadLastWorkspaceOnStartup() {
        return loadLastWorkspaceOnStartup.get();
    }

    /**
     * The loadLastWorkspaceOnStartup property
     *
     * @return the loadLastWorkspaceOnStartup property
     */
    public BooleanProperty loadLastWorkspaceOnStartupProperty() {
        return loadLastWorkspaceOnStartup;
    }

    /**
     * Sets to true if  the most recently saved workspace should be re-opned on startup, false otherwise
     *
     * @param loadLastWorkspaceOnStartup true if  the most recently saved workspace should be re-opned on startup, false otherwise
     */
    public void setLoadLastWorkspaceOnStartup(boolean loadLastWorkspaceOnStartup) {
        this.loadLastWorkspaceOnStartup.set(loadLastWorkspaceOnStartup);
    }

    /**
     * Remove a path from the list of recently opened files
     *
     * @param value a path to remove from the list of recently opened files
     */
    public void removeFromRecentFiles(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value parameter cannot be null");
        }
        if (recentFiles.contains(value)) {
            recentFiles.remove(value);
            prefs.put(RECENT_FILES, recentFiles.stream().collect(Collectors.joining("|")));
        }
    }

    /**
     * Puts a path into the list of recently opened files
     *
     * @param value a path to put into the list of recently opened files
     */
    public void putToRecentFiles(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value parameter cannot be null");
        }
        if (recentFiles.contains(value)) {
            recentFiles.remove(value);
        }
        recentFiles.addFirst(value);
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.removeLast();
        }
        prefs.put(RECENT_FILES, recentFiles.stream().collect(Collectors.joining("|")));
    }

    /**
     * Gets the list of recently opened files
     *
     * @return the list of recently opened files
     */
    public Collection<String> getRecentFiles() {
        return recentFiles;
    }

    /**
     * Returns the version information held in the containing jar's manifest
     *
     * @return the version information held in the containing jar's manifest
     */
    public String getManifestVersion() {
        if (manifest != null) {
            String[] keys = new String[]{"Specification-Version", "Implementation-Version"};
            for (String key : keys) {
                String value = manifest.getMainAttributes().getValue(key);
                if (value != null) {
                    return value;
                }
            }
        }
        return "unknown";
    }

    /**
     * Returns a list of system properties
     *
     * @return a list of system properties
     */
    public List<SysInfoProperty> getSysInfoProperties() {
        Runtime rt = Runtime.getRuntime();
        double usedMB = ((double) rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        double percentUsage = (((double) rt.totalMemory() - rt.freeMemory()) / rt.totalMemory()) * 100;

        List<SysInfoProperty> sysInfo = new ArrayList<>();
        sysInfo.add(new SysInfoProperty("binjr version", getManifestVersion()));
        sysInfo.add(new SysInfoProperty("Java version", System.getProperty("java.version")));
        sysInfo.add(new SysInfoProperty("Java vendor", System.getProperty("java.vendor")));
        sysInfo.add(new SysInfoProperty("Java VM name", System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.version") + ")"));
        sysInfo.add(new SysInfoProperty("Java home", System.getProperty("java.home")));
        sysInfo.add(new SysInfoProperty("Operating System", System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")"));
        sysInfo.add(new SysInfoProperty("System Architecture", System.getProperty("os.arch")));
        sysInfo.add(new SysInfoProperty("JVM Heap Max size", String.format("%.0f MB", (double) rt.maxMemory() / 1024 / 1024)));
        sysInfo.add(new SysInfoProperty("JVM Heap Usage", String.format("%.2f%% (%.0f/%.0f MB)", percentUsage, usedMB, (double) rt.totalMemory() / 1024 / 1024)));
        return sysInfo;
    }

    private Manifest getManifest() {
        String className = this.getClass().getSimpleName() + ".class";
        String classPath = this.getClass().getResource(className).toString();
        if (classPath.startsWith("jar")) {
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            try {
                return new Manifest(new URL(manifestPath).openStream());
            } catch (IOException e) {
                logger.error("Error extracting manifest from jar", e);
            }
        }
        logger.warn("Could not extract MANIFEST from jar!");
        return null;
    }
}
