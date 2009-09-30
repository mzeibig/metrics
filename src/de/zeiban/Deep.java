package de.zeiban;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ClassParser;

import java.io.File;
import java.io.IOException;

/**
 * Mißt die Verebungstiefe.
 * Die Verteilung der Vererbungstiefe zeigt auf, wie intensiv die Möglichkeiten der
 * Vererbung genutzt werden. Eine zu starke Nutzung von Vererbung führt zu starrem,
 * zerbrechlichem Code. Oftmals bietet sich statt der Verwendung von Vererbung die
 * Delegation an. In Java Programmen ist häufig zu beobachten, dass große
 * Vererbungstiefen bei Exceptions auftreten. Dies ist meistens unproblematisch.
 * Weisen sehr viele Klassen nur eine Vererbungstiefe von 1 auf (sie sind direkte
 * Nachkommen von java.lang.Object), so ist dies oft ein Zeichen für mangelnden
 * Einsatz der Polymorphie. Dies äußert sich auch oft durch duplizierten Quelltext.
 * Bei solchen Klassen ist zu untersuchen, ob diese tatsächlich lediglich
 * Erweiterungen von java.lang.Object sind oder evtl die Polymorphie durch
 * Implementierung von Interfaces zustande kommt.
 *
 * @author mzeibig
 * @version $Revision: 1.2 $
 * @created Mar 16, 2004 4:31:43 PM
 */
public class Deep extends AntMetrics {

    /**
     * Startet die Abarbeitung.
     *
     * @throws org.apache.tools.ant.BuildException
     */
    @Override
    public void execute() throws BuildException {
        try {
            initMetric();
            fw.write("  <deep>\n");
            printIt(rootDir);
            fw.write("  </deep>\n");
            writeStats(zaehlerMap);
            writeFooter();
        } catch (IOException e) {
            throw new BuildException("Fehler: " + e.getMessage(), e);
        }
    }

    @Override
    String getRoot() {
        return "deepjava";
    }

    @Override
    void printIt(final File dir) throws IOException {
        final String[] files = getClassfiles(dir);
        for (final String file : files) {
            int deep = 0;
            final JavaClass cf = new ClassParser(dir.getAbsolutePath() + File.separator + file).parse();
            if (!ignore(cf.getClassName())) {
                fw.write("    <class name=\"" + cf.getClassName() + "\">");
                String superclassName = cf.getSuperclassName();
                deep++;
                while (!"java.lang.Object".equals(superclassName)) {
                    final JavaClass superclass = getForName(superclassName);
                    if (superclass != null) {
                        superclassName = superclass.getSuperclassName();
                        deep++;
                    } else {
                        log("Kann Klasse '" + superclassName + "' nicht laden.", Project.MSG_ERR);
                        break;
                    }
                }
                fw.write(deep + "</class>\n");
                if (zaehlerMap.containsKey(deep)) {
                    int value = zaehlerMap.get(deep);
                    zaehlerMap.put(deep, ++value);
                } else {
                    zaehlerMap.put(deep, 1);
                }
            }
        }
        forAllSubdirsRecurse(dir);
    }

    /**
     * Test mit sich selbst.
     *
     * @param args nix
     */
    public static void main(final String[] args) {
        final Deep deep = new Deep();
        deep.setTaskName("Deep");
        deep.setOutputFile(new File("ausgabe-test.xml"));
        deep.setDirname("./out");
        deep.createClasspath();
        deep.init();
        deep.execute();
    }

}