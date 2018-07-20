// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_RUNTIME
// FULL_JDK

package foo

external class NativeClass {
    fun plus(x : Int, y : Int): Int
}

class UseNativeClass {
    companion object {
        @JvmStatic
        fun use(native: NativeClass?): NativeClass? {
            if (native != null) {
                throw Exception("Strange ${native}")
            }
            return native
        }
    }
}

fun box(): String {
    val nul : NativeClass? = UseNativeClass.use(null)
    if (nul != null) {
        return "Expecting null NativeClass: ${nul}"
    }
    val str : String? = nul as String?
    if (str != null) {
        return "Expecting null as String: ${str}"
    }

    val any : Any = "anything"
    if (any is NativeClass) {
        return "anything can be a native class"
    }

    val useNativeClass = UseNativeClass::class.java
    try {
        val method = useNativeClass.getMethod("use", NativeClass::class.java)
        if (method.getParameterCount() != 1) {
            return "Expecting one parameter but was: ${method.getParameters()}"
        }
        if (method.getParameterTypes()[0].getName() != "java.lang.Object") {
            return "Expecting external class to be replaced by Object parameter: ${method.getParameterTypes()[0].getName()}"
        }
    } catch (ex: NoSuchMethodException) {
        return "Method not found in ${useNativeClass.getMethods()}"
    }

    val nativeClass = Class.forName("foo.NativeClass")
    try {
        val method = nativeClass.getMethod("plus", Object::class.java, Integer.TYPE, Integer.TYPE)
        if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            return "External methods should be static: $method"
        }
        if (method.getParameterCount() != 3) {
            return "Expecting three parameter but was: ${method.getParameters()}"
        }
        if (method.getParameterTypes()[0].getName() != "java.lang.Object") {
            return "Expecting external class to be replaced by Object parameter: ${method.getParameterTypes()[0].getName()}"
        }
    } catch (ex: NoSuchMethodException) {
        return "Method not found in ${nativeClass.getName()}"
    }
    return "OK"
}
