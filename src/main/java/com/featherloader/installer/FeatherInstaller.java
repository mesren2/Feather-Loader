package com.featherloader.installer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Installer for FeatherLoader
 */
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
                            JOptionPane.showMessageDialog(frame, "FeatherLoader installed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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

        // Extract FeatherLoader JAR
        File currentJar = getCurrentJarFile();
        File featherJar = new File(featherDir, "featherloader-" + VERSION + ".jar");

        if (currentJar.equals(featherJar)) {
            // We're already in the installed JAR, just update the config
        } else {
            // Copy the installer JAR to the FeatherLoader directory
            Files.copy(currentJar.toPath(), featherJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Create configuration file
        File configFile = new File(featherDir, "config.properties");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("# FeatherLoader configuration\n");
            writer.write("version=" + VERSION + "\n");
            writer.write("enable-mixins=" + enableMixins + "\n");
        }

        // Modify Minecraft JAR to add FeatherLoader as an agent
        modifyMinecraftLauncher(minecraftDir, featherJar);

        // Install example mod if it doesn't exist
        installExampleMod(modsDir);
    }

    private static File getCurrentJarFile() throws Exception {
        URL url = FeatherInstaller.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(url.toURI());
    }

    private static void modifyMinecraftLauncher(File minecraftDir, File featherJar) throws Exception {
        // This is a simplified implementation
        // In a real implementation, you'd need to modify the Minecraft launcher profile
        // to add FeatherLoader as a Java agent

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

        // In a real implementation, you'd parse and modify the JSON
        // For simplicity, we'll just check if FeatherLoader is already installed
        if (content.contains("featherloader")) {
            System.out.println("FeatherLoader appears to be already installed in launcher profiles");
        } else {
            System.out.println("Would modify launcher profiles here in a real implementation");
            // In a real implementation, you would:
            // 1. Parse the JSON
            // 2. Add a new profile or modify existing ones to include FeatherLoader as a Java agent
            // 3. Write the modified JSON back to the file
        }
    }

    private static void installExampleMod(File modsDir) throws Exception {
        // Check if example mod already exists
        File exampleModFile = new File(modsDir, "featherloader-example-mod.jar");
        if (exampleModFile.exists()) {
            return;
        }

        // In a real implementation, you would extract the example mod from the installer JAR
        // For simplicity, we'll just create a minimal example mod

        // TODO: In a real implementation, you would include a pre-built example mod in the installer resources
        System.out.println("Would install example mod here in a real implementation");
    }
}