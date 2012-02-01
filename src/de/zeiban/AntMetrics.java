package de.zeiban;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.Repository;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Superklasse für die einzelnen Metriken.
 *
 * @author mzeibig
 * @version $Revision: 1.3 $
 * @created Mar 30, 2004 12:39:39 PM
 */
public abstract class AntMetrics extends org.apache.tools.ant.Task {

    /**
     * enthält alle zu ignorierenden Klassen/Packages.
     */
    private final Set<String> ignore = new TreeSet<String>();

    protected FileWriter fw;

    private String dirname;

    private File outputFile;

    File rootDir;

    private final CommandlineJava commandline = new CommandlineJava();

    private Path path;

    protected final Map<Integer, Integer> zaehlerMap = new HashMap<Integer, Integer>();    

    /**
     * bietete einen Cache für Class-Objekte.
     */
    private final Map<String, JavaClass> forNameCache = new HashMap<String, JavaClass>();

    /**
     * Prüft, ob eine Klasse ignoriert werden soll.
     *
     * @param cn der Name der Klasse
     * @return true, wenn die Klasse ignoriert werden soll.
     */
    boolean ignore(final String cn) {
        // ignoriere inner classes
        if (cn.indexOf('$') >= 0) {
            return true;
        }
        for (final String in : ignore) {
            if (cn.startsWith(in)) {
                return true;
            }
        }
        return false;
    }

    String getDirname() {
        return this.dirname;
    }

    void setDirname(final String dirname) {
        log("Dirname:" + dirname, Project.MSG_INFO);
        this.dirname = dirname;
    }

    /**
     * The output file name.
     *
     * @param outputFile die Ausgabedatei
     */
    void setOutputFile(final File outputFile) {
        log("OutputFile:" + outputFile, Project.MSG_INFO);
        this.outputFile = outputFile;
    }

    File getOutputFile() {
        return outputFile;
    }

    /**
     * Liefert alle Unterverzeichnisse zu einem Verzeichnis.
     *
     * @param dir das Verzeichnis
     * @return die Unterverzeichnisse
     */
    File[] getSubdirs(final File dir) {
        return dir.listFiles(new FileFilter() {
            public boolean accept(final File pathname) {
                return pathname.isDirectory();
            }
        });
    }

    /**
     * Schreibt den oberen Teil des XML-Files.
     * Dieser Teil ist bei allen Metriken gleich. Lediglich das Root-Element
     * ist unterschiedlich und wird über eine abstrakte Methode ermittelt, die
     * von den Metriken zu implementieren ist.
     *
     * @throws java.io.IOException im Fehlerfall
     */
    void writeHeader() throws IOException {
        this.fw.write("<?xml version=\"1.0\"?>\n");
        this.fw.write("<" + getRoot() + ">\n");
        this.fw.write("  <date>" + new SimpleDateFormat("dd.MM.yyyy").format(new Date()) + "</date>\n");
        this.fw.write("  <time>" + new SimpleDateFormat("HH.mm.ss").format(new Date()) + "</time>\n");
    }

    /**
     * Schreibt den unteren Teil des XML-Files.
     * Dieser Teil ist bei allen Metriken gleich. Lediglich das Root-Element
     * ist unterschiedlich und wird über eine abstrakte Methode ermittelt, die
     * von den Metriken zu implementieren ist.
     *
     * @throws java.io.IOException im Fehlerfall
     */
    void writeFooter() throws IOException {
        this.fw.write("</" + getRoot() + ">\n");
        this.fw.flush();
        this.fw.close();
    }

    /**
     * Liefert den Namen des Root-Elements als String.
     * @return der Name des Root-Elements
     */
    abstract String getRoot();

    /**
     * Liefert alle .class Dateien des gegebenen Verzeichnisses.
     *
     * @param dir das zu durchsuchende Verzeichnis
     * @return ein Array der Namen
     */
    String[] getClassfiles(final File dir) {
        final String[] files = dir.list(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".class");
            }
        });
        return files;
    }

    /**
     * Füllt die Liste der zu ignorierenden Packages.
     * Dazu wird eine Datei angezogen, deren Namen abhängig vom Namen
     * der Metrik-Implementierung ist.
     *
     * @throws java.io.IOException im Fehlerfall
     */
    private void fillIgnored() throws IOException {
        final InputStream is = ClassLoader.getSystemResourceAsStream(getRoot() + ".ignore");
        if (is != null) {
            final InputStreamReader isr = new InputStreamReader(is);
            final LineNumberReader lnr = new LineNumberReader(isr);
            String il;
            while ((il = lnr.readLine()) != null) {
                log("add inore: " + il, Project.MSG_VERBOSE);
                this.ignore.add(il);
            }
        }
    }

    abstract void printIt(final File dir) throws IOException;

    /**
     * Rekursive Verarbeitung aller (Unter-)Verzeichnisse.
     * Ausgenommen werden alle Verzeichnisse, die auf 'test' enden.
     *
     * @param dir das aktuelle Verzeichnis
     * @throws java.io.IOException im Fehlerfall
     */
    void forAllSubdirsRecurse(final File dir) throws IOException {
        final File[] subdirs = getSubdirs(dir);
        for (final File subdir : subdirs) {
            printIt(subdir);
        }
    }

    @Deprecated
    void writeStats(final int[] zaehler) throws IOException {
        writeStats(zaehler, "stats");
    }

    @Deprecated
    void writeStats(final int[] zaehler, final String elementName) throws IOException {
        this.fw.write("  <" + elementName + ">\n");
        for (int i = 0; i < zaehler.length; i++) {
            this.fw.write("    <sum order=\"" + i + "\">" + zaehler[i] + "</sum>\n");
        }
        this.fw.write("  </" + elementName + ">\n");
    }

    /**
     * Schreibt die Statistiken zur analysierten Metrik.
     *
     * @param zaehler Verteilung
     * @throws IOException im Fehlerfall
     */
    void writeStats(final Map<Integer, Integer> zaehler) throws IOException {
        writeStats(zaehler, "stats");
    }

    /**
     * Schreibt die Statistiken zur analysierten Metrik mit einem gegebenen Rootelement.
     *
     * @param zaehler Verteilung
     * @param elementName Name des Elementes
     * @throws IOException im Fehlerfall
     */
    void writeStats(final Map<Integer, Integer> zaehler, final String elementName) throws IOException {
        this.fw.write("  <" + elementName + ">\n");
        final Integer maxvalue = Collections.max(zaehler.keySet());
        for (int i = 0; i <= maxvalue; i++) {
            final Integer count = zaehler.get(i);
            this.fw.write("    <sum order=\"" + i + "\">" + (count==null?0:count) + "</sum>\n");
        }
        this.fw.write("  </" + elementName + ">\n");
    }

    void initMetric() throws IOException {
        log("start", Project.MSG_INFO);
        fillIgnored();
        this.fw = new FileWriter(getOutputFile());
        this.rootDir = new File(getDirname());
        final String pathStr = path.toString();
        final ClassPath classPath = new ClassPath(pathStr);
        Repository.setRepository(SyntheticRepository.getInstance(classPath));
        writeHeader();
    }

    /**
     * Adds a path to the classpath.
     * @return der Pfad
     */
    Path createClasspath() {
        this.path = commandline.createClasspath(getProject()).createPath();
        return path;
    }

    /**
     * Liefert ein Klassenobjekt zu einem Klassennamen.
     *
     * @param className Name der Klasse
     * @return Klassenobjekt
     */
    JavaClass getForName(final String className) {
        if (this.forNameCache.containsKey(className)) {
            return this.forNameCache.get(className);
        } else {
            try {
                final JavaClass cf = Repository.lookupClass(className);
                this.forNameCache.put(className, cf);
                return cf;
            } catch (ClassNotFoundException e) {
                // wir wollen keine Exception.
                return null;
            }
        }
    }
}
