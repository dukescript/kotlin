// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_RUNTIME
// FULL_JDK

package foo

external class NativeClass {
    fun plus(x : Int, y : Int): Int
}

external val native: NativeClass

class UseNativeClass {
    companion object {
        @JvmStatic
        fun use(native: NativeClass?): NativeClass? {
            if (native != null) {
                throw Exception("Strange ${native}")
            }
            return native
        }

        @JvmStatic
        fun checkField(): String? {
            val realNative: NativeClass? = native
            if (realNative != null) {
                return "Nobody sets the realNative value: ${realNative}"
            }
            return null
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

    val moduleClass = Class.forName("foo.ExternalClassKt")
    try {
        val method = moduleClass.getMethods().find {
            it.getName() == "getNative"
        }
        if (method == null) {
            throw NoSuchMethodException()
        }
        if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            return "Property getters should be static: $method"
        }
        if (method.getParameterCount() != 0) {
            return "Expecting no parameters but was: ${method.getParameters()}"
        }
        if (method.getReturnType().getName() != "java.lang.Object") {
            return "Expecting returned external class to be replaced by Object parameter: ${method.getParameterTypes()[0].getName()}"
        }

        moduleClass.getDeclaredFields().forEach {
            if (it.getName() == "native") {
                return "there should be no field: ${it}"
            }
        }
    } catch (ex: NoSuchMethodException) {
        return "Method not found in ${moduleClass.getName()} found ${java.util.Arrays.toString(moduleClass.getMethods())}"
    }

    try {
        val readNativeField = UseNativeClass.checkField()
        if (readNativeField != null) {
            return readNativeField
        }
    } catch (ex: java.lang.UnsatisfiedLinkError) {
        var exClass = ex::class
        if (!exClass.java.getName().equals("java.lang.UnsatisfiedLinkError")) {
            return "Unexpected error when calling native method: ${exClass}"
        }
    }

    return "OK"
}
