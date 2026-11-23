package com.github.laxika.magicalvibes.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shrinking ratchet for effect-cleanup-refactor.md: the number of effect records in {@code model/effect/} and of effect
 * handler classes in {@code service/effect/{normalfx,mayfx,staticfx}} may only go down. A new card is built from the
 * existing verbs, subjects and wrappers; when a refactor step deletes classes it lowers the baselines here to the new
 * counts. {@code scripts/effect-inventory.py} prints the same two numbers on its last summary line.
 *
 * <p>A record is a file whose top-level declaration is {@code public record <FileName>}; a handler class is a file whose
 * name ends in {@code Handler.java}.
 */
class EffectCountRatchetTest {

    private static final int RECORD_BASELINE = 4140;
    private static final int HANDLER_BASELINE = 3381;

    private static final String EFFECT_PKG =
            "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect";
    private static final String HANDLER_ROOT =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/effect";
    private static final List<String> HANDLER_DIRS = List.of("normalfx", "mayfx", "staticfx");

    private static final Pattern RECORD_DECLARATION = Pattern.compile(
            "(?m)^public\\s+(?:(?:sealed|non-sealed|abstract|final|static)\\s+)*record\\s+(\\w+)");

    @Test
    @DisplayName("The number of effect records does not grow above its baseline")
    void effectRecordCountDoesNotGrow() throws IOException {
        long records;
        try (Stream<Path> files = Files.list(locateRepoRoot().resolve(EFFECT_PKG))) {
            records = files.filter(path -> fileNameOf(path).endsWith(".java"))
                    .filter(EffectCountRatchetTest::declaresRecordNamedAfterFile)
                    .count();
        }

        assertThat(records)
                .withFailMessage("model/effect/ has %d records, above the baseline of %d. Build the card from existing "
                                + "effects (see effect-cleanup-refactor.md and scripts/effect-inventory.py --family); "
                                + "if a refactor step deleted records, lower RECORD_BASELINE to the new count instead.",
                        records, RECORD_BASELINE)
                .isLessThanOrEqualTo(RECORD_BASELINE);
    }

    @Test
    @DisplayName("The number of effect handler classes does not grow above its baseline")
    void effectHandlerCountDoesNotGrow() throws IOException {
        Path handlerRoot = locateRepoRoot().resolve(HANDLER_ROOT);
        long handlers = 0;
        for (String dir : HANDLER_DIRS) {
            try (Stream<Path> files = Files.walk(handlerRoot.resolve(dir))) {
                handlers += files.filter(Files::isRegularFile)
                        .filter(path -> fileNameOf(path).endsWith("Handler.java"))
                        .count();
            }
        }

        assertThat(handlers)
                .withFailMessage("service/effect/{normalfx,mayfx,staticfx} has %d handler classes, above the baseline "
                                + "of %d. Build the card from existing effects; if a refactor step deleted handlers, "
                                + "lower HANDLER_BASELINE to the new count instead.",
                        handlers, HANDLER_BASELINE)
                .isLessThanOrEqualTo(HANDLER_BASELINE);
    }

    private static boolean declaresRecordNamedAfterFile(Path path) {
        String name = fileNameOf(path);
        var matcher = RECORD_DECLARATION.matcher(readText(path));
        return matcher.find() && matcher.group(1).equals(name.substring(0, name.length() - ".java".length()));
    }

    /** Walk up from the test working directory until a Gradle settings file is found. */
    private static Path locateRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        for (Path p = dir; p != null; p = p.getParent()) {
            if (Files.exists(p.resolve("settings.gradle.kts")) || Files.exists(p.resolve("settings.gradle"))) {
                return p;
            }
        }
        throw new IllegalStateException(
                "Could not locate repo root (no settings.gradle[.kts]) walking up from " + dir);
    }

    private static String fileNameOf(Path path) {
        Path name = path.getFileName();
        return name == null ? "" : name.toString();
    }

    private static String readText(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
