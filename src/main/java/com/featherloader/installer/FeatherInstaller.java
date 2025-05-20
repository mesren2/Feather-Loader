package com.featherloader.installer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.json.JSONObject;

public class FeatherInstaller {
    private static final String TITLE = "FeatherLoader Installer";
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame(TITLE + " v" + VERSION);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Logo
            ImageIcon icon = null;
            try {
                URL logoUrl = FeatherInstaller.class.getResource("/featherloader-logo.png");
                if (logoUrl != null) {
                    icon = new ImageIcon(logoUrl);
                    JLabel logoLabel = new JLabel(icon);
                    logoLabel.setHorizontalAlignment(JLabel.CENTER);
                    panel.add(logoLabel, BorderLayout.NORTH);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Center panel
            JPanel centerPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Minecraft directory
            JPanel dirPanel = new JPanel(new BorderLayout(5, 0));
            JLabel dirLabel = new JLabel("Minecraft Directory:");
            JTextField dirField = new JTextField();

            // Try to detect default Minecraft directory
            String osName = System.getProperty("os.name").toLowerCase();
            String userHome = System.getProperty("user.home");
            File defaultDir;

            if (osName.contains("win")) {
                defaultDir = new File(System.getenv("APPDATA"), ".minecraft");
            } else if (osName.contains("mac")) {
                defaultDir = new File(userHome, "Library/Application Support/minecraft");
            } else {
                defaultDir = new File(userHome, ".minecraft");
            }

            if (defaultDir.exists()) {
                dirField.setText(defaultDir.getAbsolutePath());
            }

            JButton browseButton = new JButton("Browse");
            browseButton.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (dirField.getText() != null && !dirField.getText().isEmpty()) {
                    chooser.setCurrentDirectory(new File(dirField.getText()));
                }

                int result = chooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    dirField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            });

            dirPanel.add(dirLabel, BorderLayout.NORTH);
            dirPanel.add(dirField, BorderLayout.CENTER);
            dirPanel.add(browseButton, BorderLayout.EAST);

            // Mixin support
            JCheckBox mixinCheckbox = new JCheckBox("Enable Mixin support", true);
            JLabel mixinLabel = new JLabel("<html><small>Mixins allow advanced modding by modifying Minecraft classes directly</small></html>");

            // Install button
            JButton installButton = new JButton("Install FeatherLoader");
            installButton.addActionListener(e -> {
                String mcDir = dirField.getText();
                if (mcDir == null || mcDir.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please select your Minecraft directory", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean enableMixins = mixinCheckbox.isSelected();

                // Disable UI during installation
                installButton.setEnabled(false);
                dirField.setEnabled(false);
                browseButton.setEnabled(false);
                mixinCheckbox.setEnabled(false);

                // Install in a background thread
                new Thread(() -> {
                    try {
                        install(new File(mcDir), enableMixins);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "FeatherLoader installed successfully! Please restart the Minecraft Launcher.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            frame.dispose();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "Installation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            installButton.setEnabled(true);
                            dirField.setEnabled(true);
                            browseButton.setEnabled(true);
                            mixinCheckbox.setEnabled(true);
                        });
                    }
                }).start();
            });

            centerPanel.add(dirPanel, gbc);
            centerPanel.add(mixinCheckbox, gbc);
            centerPanel.add(mixinLabel, gbc);

            panel.add(centerPanel, BorderLayout.CENTER);
            panel.add(installButton, BorderLayout.SOUTH);

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }

    private static void install(File minecraftDir, boolean enableMixins) throws Exception {
        if (!minecraftDir.exists() || !minecraftDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid Minecraft directory");
        }

        // Create FeatherLoader directory
        File featherDir = new File(minecraftDir, "featherloader");
        if (!featherDir.exists()) {
            featherDir.mkdirs();
        }

        // Create mods directory
        File modsDir = new File(minecraftDir, "feather-mods");
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }

        // Get current JAR file
        File currentJar = getCurrentJarFile();
        File featherJar = new File(featherDir, "featherloader-" + VERSION + ".jar");

        // Copy the JAR to the installation directory
        Files.copy(currentJar.toPath(), featherJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Create configuration file
        File configFile = new File(featherDir, "config.properties");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("# FeatherLoader configuration\n");
            writer.write("version=" + VERSION + "\n");
            writer.write("enable-mixins=" + enableMixins + "\n");
        }

        // Copy to libraries folder (needed by Minecraft)
        File librariesDir = new File(minecraftDir, "libraries/com/featherloader/featherloader/" + VERSION);
        librariesDir.mkdirs();

        File libraryJar = new File(librariesDir, "featherloader-" + VERSION + ".jar");
        Files.copy(featherJar.toPath(), libraryJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Create a new launcher profile
        createLauncherProfile(minecraftDir, enableMixins);

        // Install example mod
        installExampleMod(modsDir);
    }

    private static File getCurrentJarFile() throws Exception {
        URL url = FeatherInstaller.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(url.toURI());
    }

    private static void createLauncherProfile(File minecraftDir, boolean enableMixins) throws Exception {
        File launcherProfilesJson = new File(minecraftDir, "launcher_profiles.json");

        if (!launcherProfilesJson.exists()) {
            throw new FileNotFoundException("Minecraft launcher profiles not found");
        }

        // Create a backup
        File backupFile = new File(minecraftDir, "launcher_profiles.json.backup");
        if (!backupFile.exists()) {
            Files.copy(launcherProfilesJson.toPath(), backupFile.toPath());
        }

        // Read the launcher profiles
        String content = new String(Files.readAllBytes(launcherProfilesJson.toPath()));
        JSONObject root = new JSONObject();

        if (!root.has("profiles")) {
            root.put("profiles", new JSONObject());
        }

        JSONObject profiles = (JSONObject) root.get("profiles");

        // Create a profile ID that's unique to this version of FeatherLoader
        String profileId = "featherloader-" + VERSION.replace('.', '_');

        // Create our profile
        JSONObject profile = new JSONObject();
        profile.put("name", "FeatherLoader " + VERSION + " for 1.21");
        profile.put("type", "custom");
        profile.put("created", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        profile.put("lastUsed", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        profile.put("icon", "Furnace");
        profile.put("lastVersionId", "1.21"); // Change this to match your target version

        // Set up JVM arguments to load our launcher
        String jvmArgs = "-Xmx2G";
        if (enableMixins) {
            jvmArgs += " -Dfeatherloader.mixins.enabled=true";
        }
        profile.put("javaArgs", jvmArgs);

        // Set the main class to our launcher
        profile.put("mainClass", "com.featherloader.launcher.FeatherLauncher");

        // Add the profile to the launcher profiles
        profiles.put(profileId, profile);

        // Write the updated launcher profiles
        try (FileWriter writer = new FileWriter(launcherProfilesJson)) {
            writer.write("2");
        }
    }

    private static void installExampleMod(File modsDir) throws Exception {
        // Create a simple example mod
        File exampleModFile = new File(modsDir, "featherloader-example-mod.jar");

        if (exampleModFile.exists()) {
            // Example mod already exists
            return;
        }

        // In a real implementation, you'd extract the example mod from resources
        // For simplicity, we'll just log that we would create it
        System.out.println("Would create example mod in: " + exampleModFile.getAbsolutePath());
    }
}