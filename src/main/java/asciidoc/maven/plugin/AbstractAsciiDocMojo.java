/*
 * Copyright (c) 2011-2012 Charbel Bitar.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-2.0.html
 * 
 * Contributors:
 *     cbitar - initial API and implementation
 */
package asciidoc.maven.plugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;

/**
 * Abstract AsciiDoc Mojo.
 * 
 * @author cbitar
 */
public abstract class AbstractAsciiDocMojo extends AbstractMojo {

    /**
     * AsciiDoc home.
     * 
     * @parameter
     */
    protected File asciiDocHome;

    /**
     * No-arg constructor.
     */
    public AbstractAsciiDocMojo() {
        try {
            File jarFile = new File(AbstractAsciiDocMojo.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            if (getLog().isDebugEnabled()) getLog().debug("sourceJarFile: " + jarFile.getAbsolutePath());
            if (asciiDocHome == null) {
                ZipEntry zipEntry = null;
                String zipEntryName = null;
                ZipFile jarZipFile = new ZipFile(jarFile);
                Enumeration<? extends ZipEntry> e = jarZipFile.entries();
                while (e.hasMoreElements()) {
                    zipEntry = (ZipEntry) e.nextElement();
                    zipEntryName = zipEntry.getName();
                    if (zipEntryName.startsWith("asciidoc") && zipEntryName.endsWith(".zip")) {
                        if (getLog().isInfoEnabled()) getLog().info("Found AsciiDoc in " + zipEntryName);
                        asciiDocHome = new File(jarFile.getParent(), FilenameUtils.removeExtension(zipEntryName));
                        break;
                    }
                }
                if (asciiDocHome != null && !asciiDocHome.exists()) {
                    unzipEntry(jarZipFile, zipEntry, jarFile.getParentFile());
                    File asciiDocArchive = new File(jarFile.getParent(), zipEntryName);
                    unzipArchive(asciiDocArchive, jarFile.getParentFile());
                    asciiDocArchive.deleteOnExit();
                }
                if (getLog().isInfoEnabled()) getLog().info("asciiDocHome: " + asciiDocHome);
            }
        } catch (URISyntaxException use) {
            getLog().error(use.getMessage(), use);
            // don't throw use;
        } catch (ZipException ze) {
            getLog().error(ze.getMessage(), ze);
            // don't throw ze;
        } catch (IOException ioe) {
            getLog().error(ioe.getMessage(), ioe);
            // don't throw ioe;
        }
    }

    public File getAsciiDocHome() {
        return asciiDocHome;
    }

    public void setAsciiDocHome(File asciiDocHome) {
        this.asciiDocHome = asciiDocHome;
    }

    private void unzipArchive(File archive, File outputDir) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
        } catch (Exception e) {
            getLog().error("Error while extracting file " + archive, e);
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry zipEntry, File outputDir) throws IOException {
        if (zipEntry.isDirectory()) {
            createDir(new File(outputDir, zipEntry.getName()));
            return;
        }
        File outputFile = new File(outputDir, zipEntry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }
        if (getLog().isDebugEnabled()) getLog().debug("Extracting " + zipEntry);
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(zipEntry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        if (getLog().isDebugEnabled()) getLog().debug("Creating dir " + dir.getName());
        if (!dir.mkdirs()) throw new RuntimeException("Can not create dir " + dir);
    }

}
