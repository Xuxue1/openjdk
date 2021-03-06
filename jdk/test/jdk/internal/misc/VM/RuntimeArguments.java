/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @summary Basic test of VM::getRuntimeArguments
 * @library /lib/testlibrary
 * @modules java.base/jdk.internal.misc
 * @run testng RuntimeArguments
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import jdk.internal.misc.VM;
import jdk.testlibrary.ProcessTools;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class RuntimeArguments {
    static final String TEST_CLASSES = System.getProperty("test.classes");

    @DataProvider(name = "options")
    public Object[][] options() {
        return new Object[][] {
            { // CLI options
              List.of("-XaddExports:java.base/jdk.internal.misc=ALL-UNNAMED",
                      "-XaddExports:java.base/jdk.internal.perf=ALL-UNNAMED",
                      "-addmods", "jdk.zipfs"),
              // expected runtime arguments
              List.of("-Djdk.launcher.addexports.0=java.base/jdk.internal.misc=ALL-UNNAMED",
                      "-Djdk.launcher.addexports.1=java.base/jdk.internal.perf=ALL-UNNAMED",
                      "-Djdk.launcher.addmods=jdk.zipfs")
            },
            { // CLI options
              List.of("-XaddExports:java.base/jdk.internal.misc=ALL-UNNAMED",
                      "-addmods", "jdk.zipfs",
                      "-limitmods", "java.compact3"),
              // expected runtime arguments
              List.of("-Djdk.launcher.addexports.0=java.base/jdk.internal.misc=ALL-UNNAMED",
                      "-Djdk.launcher.addmods=jdk.zipfs", "-Djdk.launcher.limitmods=java.compact3")
            },
        };
    };

    public static void main(String... expected) {
        String[] vmArgs = VM.getRuntimeArguments();
        if (!Arrays.equals(vmArgs, expected)) {
            throw new RuntimeException(Arrays.toString(vmArgs) +
                " != " + Arrays.toString(expected));
        }
    }

    @Test(dataProvider = "options")
    public void test(List<String> args, List<String> expected) throws Exception {
        // launch a test program
        // $ java <runtime-arguments> -classpath <cpath> RuntimeArguments <expected>

        Stream<String> options = Stream.concat(args.stream(),
            Stream.of("-classpath", TEST_CLASSES, "RuntimeArguments"));

        ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(
            Stream.concat(options, expected.stream())
                  .toArray(String[]::new)
        );
        ProcessTools.executeProcess(pb).shouldHaveExitValue(0);
    }
}
