/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cli.common;

import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.common.arguments.Argument;
import org.jetbrains.kotlin.cli.common.arguments.CommonToolArguments;
import org.jetbrains.kotlin.cli.common.arguments.ParseCommandLineArgumentsKt;

import java.lang.reflect.Field;

public class Usage {
    // The magic number 29 corresponds to the similar padding width in javac and scalac command line compilers
    private static final int OPTION_NAME_PADDING_WIDTH = 29;

    @NotNull
    public static <A extends CommonToolArguments> String render(@NotNull CLITool<A> tool, @NotNull A arguments) {
        StringBuilder sb = new StringBuilder();
        appendln(sb, "Usage: " + tool.executableScriptFileName() + " <options> <source files>");
        appendln(sb, "where " + (arguments.extraHelp ? "advanced" : "possible") + " options include:");
        for (Class<?> clazz = arguments.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                fieldUsage(sb, field, arguments.extraHelp);
            }
        }

        if (arguments.extraHelp) {
            appendln(sb, "");
            appendln(sb, "Advanced options are non-standard and may be changed or removed without any notice.");
        }

        return sb.toString();
    }

    private static void fieldUsage(@NotNull StringBuilder sb, @NotNull Field field, boolean extraHelp) {
        Argument argument = field.getAnnotation(Argument.class);
        if (argument == null) return;

        if (extraHelp != ParseCommandLineArgumentsKt.isAdvanced(argument)) return;

        int startLength = sb.length();
        sb.append("  ");
        sb.append(argument.value());

        if (!argument.shortName().isEmpty()) {
            sb.append(" (");
            sb.append(argument.shortName());
            sb.append(")");
        }

        if (!argument.valueDescription().isEmpty()) {
            sb.append(ParseCommandLineArgumentsKt.isAdvanced(argument) ? "=" : " ");
            sb.append(argument.valueDescription());
        }

        int margin = startLength + OPTION_NAME_PADDING_WIDTH - 1;
        if (sb.length() >= margin + 5) { // Break the line if it's too long
            sb.append("\n");
            margin += sb.length() - startLength;
        }
        while (sb.length() < margin) {
            sb.append(" ");
        }

        sb.append(" ");
        appendln(sb, argument.description().replace("\n", "\n" + StringsKt.repeat(" ", OPTION_NAME_PADDING_WIDTH)));
    }

    private static void appendln(@NotNull StringBuilder sb, @NotNull String string) {
        sb.append(string);
        StringsKt.appendln(sb);
    }
}
