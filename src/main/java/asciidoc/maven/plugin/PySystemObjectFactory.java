package asciidoc.maven.plugin;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySystemState;

/**
 * PySystemObjectFactory
 * 
 * A Jython object factory implementation by Jim Baker.
 * 
 * This object factory implementation has the benefit of not making use of
 * the PythonInterpreter, which is known to be poor for performance. By
 * utilizing the PySystemState, we can obtain a reference to the importer. In turn,
 * we can use the importer to obtain reference to our Jython module.
 * 
 * @author jimbaker
 */
public class PySystemObjectFactory {

    private final Class interfaceType;
    private final PyObject klass;

    // likely want to reuse PySystemState in some clever fashion since expensive to setup...
    public PySystemObjectFactory(PySystemState state, Class interfaceType, String moduleName, String className) {
        this.interfaceType = interfaceType;
        PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));
        PyObject module = importer.__call__(Py.newString(moduleName));
        klass = module.__getattr__(className);
        System.err.println("module=" + module + ",class=" + klass);
    }

    public PySystemObjectFactory(Class interfaceType, String moduleName, String className) {
        this(new PySystemState(), interfaceType, moduleName, className);
    }

    public Object createObject() {
        return klass.__call__().__tojava__(interfaceType);
    }

    public Object createObject(Object arg1) {
        return klass.__call__(Py.java2py(arg1)).__tojava__(interfaceType);
    }

    public Object createObject(Object arg1, Object arg2) {
        return klass.__call__(Py.java2py(arg1), Py.java2py(arg2)).__tojava__(interfaceType);
    }

    public Object createObject(Object arg1, Object arg2, Object arg3) {
        return klass.__call__(Py.java2py(arg1), Py.java2py(arg2), Py.java2py(arg3)).__tojava__(interfaceType);
    }

    public Object createObject(Object args[], String keywords[]) {
        PyObject convertedArgs[] = new PyObject[args.length];
        for (int i = 0; i < args.length; i++) {
            convertedArgs[i] = Py.java2py(args[i]);
        }
        return klass.__call__(convertedArgs, keywords).__tojava__(interfaceType);
    }

    public Object createObject(Object... args) {
        return createObject(args, Py.NoKeywords);
    }

}
