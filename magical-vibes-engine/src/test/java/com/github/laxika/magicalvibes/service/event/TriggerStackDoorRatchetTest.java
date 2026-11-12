package com.github.laxika.magicalvibes.service.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Counts, in every main source file outside the stack door ({@code TriggeredAbilityStackService} and its request), the
 * code that puts a triggered ability toward the stack by hand, and holds each file to the counts in the committed
 * allowlist {@value #ALLOWLIST}. The counts per file are:
 *
 * <ol>
 *   <li>{@code built}: {@code TRIGGERED_ABILITY} named as a value (building or passing an entry type), not in a
 *       comparison ({@code ==}, {@code !=}, {@code case});</li>
 *   <li>{@code queued}: calls of {@code enqueueTrigger}, {@code handOffTriggerRecord} and {@code queueMayAbility};</li>
 *   <li>{@code effects}: in a file with a count above, {@code instanceof} (or {@code .class::isInstance}) of an effect
 *       class of the domain's {@code model/effect} package, and reads of {@code .effects()}.</li>
 * </ol>
 *
 * A file whose count differs from its line, either way, fails the test, so the allowlist is always the true remainder
 * and only ever goes down. With the environment variable {@value #WRITE_VARIABLE} set, the test writes the allowlist
 * as measured instead of comparing.
 */
class TriggerStackDoorRatchetTest {

    private static final String ALLOWLIST = "magical-vibes-engine/src/test/resources/trigger-stack-door-allowlist.txt";
    private static final String WRITE_VARIABLE = "MV_RATCHET_WRITE";

    private static final Set<String> DOOR_FILES = Set.of("TriggeredAbilityStackService.java",
            "TriggeredAbilityRequest.java", "StackEntryType.java");

    private static final Pattern NAMED = Pattern.compile("\\b(?:StackEntryType\\.)?TRIGGERED_ABILITY\\b");
    private static final Pattern COMPARED = Pattern.compile(
            "[=!]=\\s*(?:StackEntryType\\.)?TRIGGERED_ABILITY\\b"
                    + "|\\b(?:StackEntryType\\.)?TRIGGERED_ABILITY\\s*[=!]="
                    + "|\\bcase\\s+(?:StackEntryType\\.)?TRIGGERED_ABILITY\\b"
                    + "|import\\s+static\\s+[\\w.]+\\.TRIGGERED_ABILITY\\s*;");
    private static final String QUEUE_METHODS = "(?:enqueueTrigger|handOffTriggerRecord|queueMayAbility)";
    private static final Pattern QUEUE_CALL = Pattern.compile(
            "(?:\\.|::)\\s*" + QUEUE_METHODS + "\\b|(?<![\\w.:])" + QUEUE_METHODS + "\\s*\\(");
    private static final Pattern QUEUE_DECLARATION = Pattern.compile(
            "\\b(?:void|StackEntry|boolean|int|[A-Z]\\w*(?:<[^>]*>)?)\\s+" + QUEUE_METHODS + "\\s*\\(");
    private static final Pattern INSTANCE_OF = Pattern.compile("\\binstanceof\\s+(?:[\\w.]+\\.)?(\\w+)");
    private static final Pattern IS_INSTANCE = Pattern.compile("\\b(\\w+)\\.class\\s*(?:::|\\.)\\s*isInstance\\b");
    private static final Pattern EFFECTS_READ = Pattern.compile("\\.effects\\(\\)");

    @Test
    void handBuiltTriggeredStackEntriesOnlyGoDown() throws IOException {
        Path root = locateRepoRoot();
        Map<String, Counts> measured = measure(root);
        Path allowlist = root.resolve(ALLOWLIST);
        if (System.getenv(WRITE_VARIABLE) != null) {
            List<String> lines = new ArrayList<>();
            lines.add("# path built queued effects (TriggerStackDoorRatchetTest); lower a count, never raise it");
            measured.forEach((path, counts) -> lines.add(path + " " + counts));
            Files.write(allowlist, lines, StandardCharsets.UTF_8);
            return;
        }
        Map<String, Counts> allowed = new TreeMap<>();
        for (String line : Files.readAllLines(allowlist, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.trim().split("\\s+");
            allowed.put(parts[0], new Counts(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])));
        }
        List<String> above = new ArrayList<>();
        List<String> below = new ArrayList<>();
        Set<String> paths = new java.util.TreeSet<>(measured.keySet());
        paths.addAll(allowed.keySet());
        for (String path : paths) {
            Counts now = measured.getOrDefault(path, Counts.NONE);
            Counts entry = allowed.getOrDefault(path, Counts.NONE);
            if (now.built > entry.built || now.queued > entry.queued || now.effects > entry.effects) {
                above.add(path + ": " + now + " > allowed " + entry);
            } else if (!now.equals(entry)) {
                below.add(path + ": " + now + " < allowed " + entry);
            }
        }
        assertThat(above)
                .withFailMessage(() -> "Triggered abilities put toward the stack outside the stack door "
                        + "(TriggeredAbilityStackService.put): " + above)
                .isEmpty();
        assertThat(below)
                .withFailMessage(() -> "Lower these lines of " + ALLOWLIST + " to the counts measured: " + below)
                .isEmpty();
    }

    private static Map<String, Counts> measure(Path root) throws IOException {
        Set<String> effectClasses;
        try (Stream<Path> files = Files.walk(root.resolve(
                "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect"))) {
            effectClasses = files.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .collect(Collectors.toSet());
        }
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
        Map<String, Counts> measured = new TreeMap<>();
        for (Path source : sources) {
            String code = strip(Files.readString(source, StandardCharsets.UTF_8));
            int built = count(NAMED, code) - count(COMPARED, code);
            int queued = count(QUEUE_CALL, code) - count(QUEUE_DECLARATION, code);
            if (built == 0 && queued == 0) {
                continue;
            }
            int effects = count(EFFECTS_READ, code);
            for (Pattern pattern : List.of(INSTANCE_OF, IS_INSTANCE)) {
                Matcher matcher = pattern.matcher(code);
                while (matcher.find()) {
                    if (effectClasses.contains(matcher.group(1))) {
                        effects++;
                    }
                }
            }
            measured.put(root.relativize(source).toString().replace('\\', '/'), new Counts(built, queued, effects));
        }
        return measured;
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

    private record Counts(int built, int queued, int effects) {

        static final Counts NONE = new Counts(0, 0, 0);

        @Override
        public String toString() {
            return built + " " + queued + " " + effects;
        }
    }
}
