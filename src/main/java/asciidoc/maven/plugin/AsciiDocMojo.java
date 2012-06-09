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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;

/**
 * AsciiDoc Mojo.
 * 
 * @goal asciidoc
 * @phase pre-site
 * @threadSafe
 * 
 * @author cbitar
 */
public class AsciiDocMojo extends AbstractAsciiDocMojo {

    /**
     * Source directory.
     * 
     * @parameter expression="${asciidoc.srcdir}" default-value="${project.resources}"
     */
    private File srcdir;

    /**
     * Output directory name.
     * 
     * @parameter expression="${asciidoc.outdir}"
     */
    private File outdir;

    /**
     * Source file.
     * 
     * @parameter expression="${asciidoc.srcfile}"
     */
    private File srcfile;

    /**
     * Output file name.
     * 
     * @parameter expression="${asciidoc.outfile}"
     */
    private File outfile;

    /**
     * Backend.
     * 
     * @parameter expression="${asciidoc.backend}" default-value="html5"
     */
    private String backend;

    /**
     * Attributes.
     * 
     * @parameter
     */
    private String[] attributes;

    /**
     * No-header-footer.
     * 
     * @parameter expression="${asciidoc.noHeaderFooter}" default-value="false"
     */
    private boolean noHeaderFooter;

    /**
     * Language.
     * 
     * @parameter expression="${asciidoc.lang}" default-value="en"
     */
    private String lang;

    public File getSrcdir() {
        return srcdir;
    }

    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }

    public File getOutdir() {
        return outdir;
    }

    public void setOutdir(File outdir) {
        this.outdir = outdir;
    }

    public File getSrcfile() {
        return srcfile;
    }

    public void setSrcfile(File srcfile) {
        this.srcfile = srcfile;
    }

    public File getOutfile() {
        return outfile;
    }

    public void setOutfile(File outfile) {
        this.outfile = outfile;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public boolean isNoHeaderFooter() {
        return noHeaderFooter;
    }

    public void setNoHeaderFooter(boolean noHeaderFooter) {
        this.noHeaderFooter = noHeaderFooter;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Execute.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (getLog().isDebugEnabled()) getLog().debug("asciiDocHome absolutePath: " + asciiDocHome.getAbsolutePath());
        PySystemState sys = Py.getSystemState();
        sys.path.append(new PyString(asciiDocHome.getAbsolutePath()));
        //
        PySystemObjectFactory asciidocFactory = new PySystemObjectFactory(sys, PyObject.class, "asciidocapi", "AsciiDocAPI");
        PyObject asciidoc = (PyObject) asciidocFactory.createObject();
        //
        if (outfile != null) {
            PyObject options = asciidoc.__getattr__("options");
            options.invoke("append", new PyString("--out-file"), new PyString(outfile.getAbsolutePath()));
            if (outdir == null) {
                outdir = outfile.getParentFile();
            }
        }
        if (outdir != null) {
            try {
                if (getLog().isDebugEnabled()) getLog().debug("Copying " + asciiDocHome.getAbsolutePath() + "/images" + " to " + outdir.getAbsolutePath() + "/images");
                FileUtils.copyDirectory(new File(asciiDocHome.getAbsolutePath(), "images"), new File(outdir.getAbsolutePath(), "images"));
            } catch (IOException ioe) {
                getLog().error(ioe.getMessage(), ioe);
                // don't throw ioe;
            }
        }
        //
        if (backend != null) {
            PyObject options = asciidoc.__getattr__("options");
            options.invoke("append", new PyString("--backend"), new PyString(backend));
        }
        //
        if (lang != null) {
            PyDictionary attributes = (PyDictionary) asciidoc.__getattr__("attributes");
            attributes.__setitem__(new PyString("lang"), new PyString(lang));
        }
        //
        asciidoc.invoke("execute", new PyString(srcfile.getAbsolutePath()));
    }

}
