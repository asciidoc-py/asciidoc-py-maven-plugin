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

	/** The location of the AsciiDoc */
    protected File asciiDocHome;

    /** No-args constructor */
    public AbstractAsciiDocMojo() {
    	init();
    }
    
    /**
     * Loads the AsciiDoc.jar file from it's location
     */
	private final void init() {
		try {
            File jarFile = new File(AbstractAsciiDocMojo.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            
            //Log the jar file location if debugging is enabled
            if (getLog().isDebugEnabled()) 
            	getLog().debug("sourceJarFile: " + jarFile.getAbsolutePath());
            
            //Set up the asciiDocHome if it is null
            if (asciiDocHome == null) {
                ZipEntry zipEntry = null;
                String zipEntryName = null;
                ZipFile jarZipFile = new ZipFile(jarFile);
                Enumeration<? extends ZipEntry> e = jarZipFile.entries(); //Get entries from the jar file
                
                //Iterate over jar file entries attempting to find the location of the ascii doc
                while (e.hasMoreElements()) {
                    zipEntry = (ZipEntry) e.nextElement();
                    zipEntryName = zipEntry.getName();
                    
                    //If the specified entry is the asciidoc archive
                    if (zipEntryName.startsWith("asciidoc") && zipEntryName.endsWith(".zip")) {
                    	
                    	//Log the discovery if logging is enabled
                        if (getLog().isInfoEnabled()) 
                        		getLog().info("Found AsciiDoc in " + zipEntryName);
                        asciiDocHome = new File(jarFile.getParent(), FilenameUtils.removeExtension(zipEntryName));
                        break;
                    }
                }
                
                //If the ascii doc home exists, unzip it.
                if (asciiDocHome != null && !asciiDocHome.exists()) {
                    unzipEntry(jarZipFile, zipEntry, jarFile.getParentFile());
                    File asciiDocArchive = new File(jarFile.getParent(), zipEntryName);
                    unzipArchive(asciiDocArchive, jarFile.getParentFile());
                    asciiDocArchive.deleteOnExit();
                }
                
                //Log the asciiDocHome if logging is enabled
                if (getLog().isInfoEnabled()) 
                	getLog().info("asciiDocHome: " + asciiDocHome);
            }
        } 
		catch (URISyntaxException | ZipException | IOException e) {
			//To prevent program halt, catch and log exceptions
            getLog().error(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the AsciiDocHome file object
	 * @return A file object that represents the AsciiDocHome
	 */
    public File getAsciiDocHome() {
        return asciiDocHome;
    }

    /**
     * Sets the AsciiDocHome
     * @param asciiDocHome The new file for the AsciiDocHome
     */
    public void setAsciiDocHome(File asciiDocHome) {
        this.asciiDocHome = asciiDocHome;
    }

    /**
     * Unzips a zip archive and places the contents of that zip archive into the specified directory.
     * @param archive The zip archive to unzip
     * @param outputDir The directory to place the contents of the archive into
     */
    private void unzipArchive(File archive, File outputDir) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            
            //Unzip the entries within the archive
            for( ZipEntry ze : zipfile.entries() ) {
            	 ZipEntry entry = (ZipEntry) ze.nextElement();
                 unzipEntry(zipfile, entry, outputDir);
            }
        } catch (Exception e) {
            getLog().error("Error while extracting file " + archive, e);
        }
    }

    /**
     * Unzips individual zip entries and places them into the output directory
     * @param zipfile The zip file the entries belong to
     * @param zipEntry The zip entry to unzip
     * @param outputDir The directory to place the unzipped files into
     */
    private void unzipEntry(ZipFile zipfile, ZipEntry zipEntry, File outputDir) {
    	//If the entry is just a directory, just create the create the directory in the output directory
        if (zipEntry.isDirectory()) {
            createDir(new File(outputDir, zipEntry.getName()));
            return;
        }
        
        File outputFile = new File(outputDir, zipEntry.getName());
        
        //Create the parent directory if it doesn't exist
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }
        
        
        if (getLog().isDebugEnabled()) 
        	getLog().debug("Extracting " + zipEntry);
        
        //Copy the unzipped entries to the output directory
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(zipEntry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        IOUtils.copy(inputStream, outputStream);
    }

    /**
     * Create the specified directory
     * @param dir The directory to create
     */
    private void createDir(File dir) {
    	//Log the action
        if (getLog().isDebugEnabled()) 
        	getLog().debug("Creating dir " + dir.getName());
        
        //Attempt to create the directories. Throw a runtime exception on failure.
        if (!dir.mkdirs()) 
        	throw new RuntimeException("Can not create dir " + dir);
    }

}
