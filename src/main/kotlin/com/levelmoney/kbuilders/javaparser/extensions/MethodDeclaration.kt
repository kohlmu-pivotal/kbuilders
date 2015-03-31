package com.levelmoney.kbuilders.javaparser.extensions

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.levelmoney.kbuilders.Config

/**
 * Created by Aaron Sarazan on 3/24/15
 * Copyright(c) 2015 Level, Inc.
 */

public fun MethodDeclaration.isBuildMethod():Boolean {
    return getName().equals("build") && !getType().toString().equals("void")
}

public fun MethodDeclaration.toKotlin(config: Config): List<String> {
    val retval = arrayListOf(baseKotlin(config))
    // Once we can reliably link TypeParameter <-> ClassOrInterfaceDeclaration, we can add a shortcut method here.
    return retval
}

public fun MethodDeclaration.baseKotlin(config: Config): String {
    assertIsBuilder()
    val builderClass = getClassOrInterface()
    val enclosing = builderClass.getTypeForThisBuilder()
    val builderName = builderClass.getName()
    val type = kotlinifyType(getParameters().first().getType().toString())
    val name = getName()
    val inline = if (config.inline) " inline " else " "
    return "public${inline}fun $enclosing.$builderName.${config.methodPrefix}$name(fn: () -> $type): $enclosing.$builderName = $name(fn())"
}

public fun MethodDeclaration.builderKotlin(config: Config): String {
    assertIsBuilder()
    val builderClass = getClassOrInterface()
    val enclosing = builderClass.getTypeForThisBuilder()
    val builderName = builderClass.getName()
    val type = kotlinifyType(getParameters().first().getType().toString())
    val name = getName()
    val inline = if (config.inline) " inline " else " "
    return "public${inline}fun $enclosing.$builderName.${config.methodPrefix}${name}Built(fn: $type.$builderName.() -> $type.$builderName): $enclosing.$builderName = $name(build$type{fn()})"
}

public fun MethodDeclaration.getClassOrInterface(): ClassOrInterfaceDeclaration {
    return getParentNode() as ClassOrInterfaceDeclaration
}

public fun kotlinifyType(type: String): String {
    return when (type) {
        "byte" -> "Byte"
        "short" -> "Short"
        "int", "Integer" -> "Int"
        "long" -> "Long"
        "float" -> "Float"
        "double" -> "Double"
        "boolean" -> "Boolean"
        "char" -> "Char"
        else -> kotlinifyOther(type)
    }
}

public fun kotlinifyOther(type: String): String {
    return when {
        type.endsWith("[]") -> kotlinifyType(type.replace("[]", "")) + "Array"
        else -> type
    }
}

public fun MethodDeclaration.assertIsBuilder() {
    getClassOrInterface().assertIsBuilder()
}
