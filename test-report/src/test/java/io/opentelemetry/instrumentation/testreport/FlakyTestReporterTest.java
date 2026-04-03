/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.testreport;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlakyTestReporterTest {
  @TempDir Path tempDir;

  @Test
  void testXxeVulnerability() throws Exception {
    Path testResultsDir = tempDir.resolve("build").resolve("test-results");
    Files.createDirectories(testResultsDir);
    Path testReport = testResultsDir.resolve("TEST-xxe.xml");

    // This payload tries to read a dummy file
    Path dummyFile = tempDir.resolve("dummy.txt");
    String secret = "secret_dummy_content_12345";
    Files.writeString(dummyFile, secret);

    String xxePayload =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE testsuites [\n"
            + "  <!ENTITY xxe SYSTEM \"file://"
            + dummyFile.toAbsolutePath()
            + "\">\n"
            + "]>\n"
            + "<testsuites tests=\"1\" skipped=\"0\" failures=\"1\" errors=\"0\" timestamp=\"2023-01-01T00:00:00\">\n"
            + "  <testcase classname=\"&xxe;\" name=\"test\" time=\"0.0\">\n"
            + "    <failure message=\"failed\"/>\n"
            + "  </testcase>\n"
            + "</testsuites>";

    Files.writeString(testReport, xxePayload);

    System.setProperty("scanPath", tempDir.toAbsolutePath().toString());

    PrintStream originalErr = System.err;
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent, true, UTF_8.name()));

    try {
      FlakyTestReporter.main();
      String output = errContent.toString(UTF_8.name());

      assertFalse(output.contains(secret), "XXE vulnerability: Entity was resolved!");
    } finally {
      System.setErr(originalErr);
      System.clearProperty("scanPath");
    }
  }
}
