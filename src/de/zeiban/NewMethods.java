package de.zeiban;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Method;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

public class NewMethods extends AntMetrics {

    /**
     * Startet die Abarbeitung.
     *
     * @throws org.apache.tools.ant.BuildException
     */
    @Override
    public void execute() throws BuildException {
        try {
            initMetric();
            fw.write("  <methods>\n");
            printIt(rootDir);
            fw.write("  </methods>\n");
            writeStats(zaehlerMap);
            writeFooter();
        } catch (IOException e) {
            throw new BuildException("Fehler: " + e.getMessage(), e);
        }
    }

    @Override
    String getRoot() {
        return "newmethods";
    }


    private boolean isInSuper(final JavaClass cf, final String methodName) {
        final String superName = cf.getSuperclassName();
        return isInClass(superName, methodName);
    }

    private boolean isInInterface(final JavaClass cf, final String methodName) throws ClassNotFoundException {
        final JavaClass[] interfaces = cf.getInterfaces();
        for (final JavaClass anInterface : interfaces) {
            final String interfaceName = anInterface.getClassName();
            if (isInClass(interfaceName, methodName)) {
                return true;
            }
        }
        return false;
    }

    private final Set<String> inClassCache = new HashSet<String>();

    private boolean isInClass(final String className, final String methodName) {
        if (inClassCache.contains(className + methodName)) {
            return true;
        } else {
            final JavaClass classFile = getForName(className);
            if (classFile == null) {
                log("Kann Klasse " + className + " nicht laden.", Project.MSG_ERR);
            } else {
                final Method[] classMethods = classFile.getMethods();
                for (final Method classMethod : classMethods) {
                    if (classMethod.getName().equals(methodName)) {
                        log("Ueberschrieben: " + methodName + " aus " + className, Project.MSG_DEBUG);
                        inClassCache.add(className + methodName);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isNewDefined(final JavaClass cf, final String methodName) throws ClassNotFoundException {
        return (!isInSuper(cf, methodName)) && (!isInInterface(cf, methodName));
    }

    private boolean isPrivate(final Method method) {
        return method.isPrivate();
    }

    private boolean isPublic(final Method method) {
        return method.isPublic();
    }

    @Override
    void printIt(final File dir) throws IOException {
        final String[] files = getClassfiles(dir);
        for (final String file : files) {
            int publics = 0;
            final JavaClass cf = new ClassParser(dir.getAbsolutePath() + File.separator + file).parse();
            if (!ignore(cf.getClassName())) {
                fw.write("    <class name=\"" + cf.getClassName() + "\">");
                final Method[] mil = cf.getMethods();
                for (final Method method : mil) {
                    //if (!isPrivate(method) /*&& (isNewDefined(cf, method.getName()))*/) {
                    if (isPublic(method)) {
                        publics++;
                    }
                }
                fw.write(publics + "</class>\n");
                if (zaehlerMap.containsKey(publics)) {
                    int value = zaehlerMap.get(publics);
                    zaehlerMap.put(publics, ++value);
                } else {
                    zaehlerMap.put(publics, 1);
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
        final NewMethods newMethods = new NewMethods();
        newMethods.setTaskName("NewMethods");
        newMethods.setOutputFile(new File("ausgabe-test.xml"));
        newMethods.setDirname("./out");
        newMethods.createClasspath();
        newMethods.init();
        newMethods.execute();
    }

}
