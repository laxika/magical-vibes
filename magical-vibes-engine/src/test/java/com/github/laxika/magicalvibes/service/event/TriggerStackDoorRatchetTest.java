package com.github.laxika.magicalvibes.service.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Holds every main source file outside the stack door to naming {@code TRIGGERED_ABILITY} only to compare an entry's
 * type ({@code ==}, {@code !=}, {@code case}): the type as a value builds or passes a triggered ability's entry, which
 * only the door builds. The compiler keeps the rest of the door's rule ({@code StackDoor}'s members are protected,
 * {@code GameData}'s queue package-private); {@code StackEntry}'s public constructors refuse the type only as they
 * run, so this test catches a route outside the door before any game does.
 */
class TriggerStackDoorRatchetTest {

    /** The door, the type itself, and the domain mechanism the door builds through. */
    private static final Set<String> DOOR_FILES = Set.of("TriggeredAbilityStackService.java", "StackEntryType.java",
            "StackDoor.java", "StackEntry.java");

    private static final Pattern NAMED = Pattern.compile("\\b(?:StackEntryType\\.)?TRIGGERED_ABILITY\\b");
    private static final Pattern COMPARED = Pattern.compile(
            "[=!]=\\s*(?:StackEntryType\\.)?TRIGGERED_ABILITY\\b"
                    + "|\\b(?:StackEntryType\\.)?TRIGGERED_ABILITY\\s*[=!]="
                    + "|\\bcase\\s+(?:StackEntryType\\.)?TRIGGERED_ABILITY\\b"
                    + "|import\\s+static\\s+[\\w.]+\\.TRIGGERED_ABILITY\\s*;");

    @Test
    void onlyTheStackDoorNamesTheTriggeredAbilityTypeAsAValue() throws IOException {
        Path root = locateRepoRoot();
        List<String> offending = new ArrayList<>();
        for (Path source : mainSources(root)) {
            String code = strip(Files.readString(source, StandardCharsets.UTF_8));
            int named = count(NAMED, code) - count(COMPARED, code);
            if (named > 0) {
                offending.add(root.relativize(source).toString().replace('\\', '/') + " (" + named + ")");
            }
        }
        assertThat(offending)
                .withFailMessage(() -> "A triggered ability's stack entry is built only by the stack door "
                        + "(TriggeredAbilityStackService); these files name TRIGGERED_ABILITY as a value: " + offending)
                .isEmpty();
    }

    private static List<Path> mainSources(Path root) throws IOException {
        List<Path> sourceRoots = new ArrayList<>();
        for (Path parent : List.of(root, root.resolve("magical-vibes-card"))) {
            try (Stream<Path> modules = Files.list(parent)) {
                modules.map(module -> module.resolve("src/main/java")).filter(Files::isDirectory)
                        .forEach(sourceRoots::add);
            }
        }
        List<Path> sources = new ArrayList<>();
        for (Path sourceRoot : sourceRoots) {
            try (Stream<Path> files = Files.walk(sourceRoot)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> !DOOR_FILES.contains(path.getFileName().toString()))
                        .forEach(sources::add);
            }
        }
        return sources;
    }

    private static int count(Pattern pattern, String code) {
        Matcher matcher = pattern.matcher(code);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /** {@code code} without comments and with string and character literals emptied. */
    private static String strip(String code) {
        StringBuilder out = new StringBuilder(code.length());
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);
            if (c == '/' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
                while (i < code.length() && code.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '/' && i + 1 < code.length() && code.charAt(i + 1) == '*') {
                int end = code.indexOf("*/", i + 2);
                i = end < 0 ? code.length() : end + 2;
                out.append(' ');
            } else if (c == '"' && code.startsWith("\"\"\"", i)) {
                int end = code.indexOf("\"\"\"", i + 3);
                i = end < 0 ? code.length() : end + 3;
                out.append("\"\"");
            } else if (c == '"' || c == '\'') {
                int j = i + 1;
                while (j < code.length() && code.charAt(j) != c && code.charAt(j) != '\n') {
                    j += code.charAt(j) == '\\' ? 2 : 1;
                }
                i = j + 1;
                out.append(c).append(c);
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    private static Path locateRepoRoot() {
        Path directory = Path.of("").toAbsolutePath();
        for (Path candidate = directory; candidate != null; candidate = candidate.getParent()) {
            if (Files.exists(candidate.resolve("settings.gradle.kts"))
                    || Files.exists(candidate.resolve("settings.gradle"))) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not locate repository root from " + directory);
    }
}
