package com.github.laxika.magicalvibes.service.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shrinking allowlist for the three engine notification surfaces replaced by domain events.
 *
 * <p>Every package family not listed here has an implicit baseline of zero. Every listed count
 * must eventually reach the explicit target of zero; until then counts may neither increase nor
 * silently decrease without updating this ledger and agent-docs/DOMAIN_EVENTS.md.
 */
class LegacyNotificationSurfaceRatchetTest {

    private static final String SERVICE_ROOT =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service";
    private static final String ROOT_FAMILY = "(root)";
    private static final int EVENTUAL_TARGET_PER_FAMILY = 0;

    private static final Map<LegacySurface, Map<String, Integer>> BASELINE = new EnumMap<>(LegacySurface.class);

    static {
        BASELINE.put(LegacySurface.BROADCAST_GAME_STATE, Map.of(
                ROOT_FAMILY, 11,
                "ability", 14,
                "effect/normalfx", 16,
                "input", 58,
                "interaction", 3,
                "spell", 3,
                "turn", 13));

        BASELINE.put(LegacySurface.SESSION_SEND, Map.of(
                ROOT_FAMILY, 8,
                "ability", 2,
                "effect/normalfx", 8,
                "interaction", 33));

        BASELINE.put(LegacySurface.LOG_AND_BROADCAST, Map.ofEntries(
                Map.entry(ROOT_FAMILY, 111),
                Map.entry("ability", 40),
                Map.entry("ability/cost", 8),
                Map.entry("aura", 4),
                Map.entry("battle", 4),
                Map.entry("battlefield", 40),
                Map.entry("combat", 78),
                Map.entry("effect", 2),
                Map.entry("effect/mayfx", 23),
                Map.entry("effect/normalfx", 1275),
                Map.entry("graveyard", 17),
                Map.entry("input", 468),
                Map.entry("interaction", 16),
                Map.entry("paradigm", 8),
                Map.entry("spell", 27),
                Map.entry("state", 8),
                Map.entry("trigger", 143),
                Map.entry("turn", 99)));
    }

    @Test
    void legacyNotificationCountsMatchTheShrinkingPackageAllowlist() throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        assertThat(serviceRoot).isDirectory();
        assertThat(EVENTUAL_TARGET_PER_FAMILY).isZero();

        Map<LegacySurface, Map<String, Integer>> current = scan(serviceRoot);
        List<String> failures = new ArrayList<>();

        for (LegacySurface surface : LegacySurface.values()) {
            Map<String, Integer> wasByFamily = BASELINE.get(surface);
            Map<String, Integer> nowByFamily = current.get(surface);
            Set<String> allFamilies = new TreeSet<>();
            allFamilies.addAll(wasByFamily.keySet());
            allFamilies.addAll(nowByFamily.keySet());

            for (String family : allFamilies) {
                int was = wasByFamily.getOrDefault(family, EVENTUAL_TARGET_PER_FAMILY);
                int now = nowByFamily.getOrDefault(family, EVENTUAL_TARGET_PER_FAMILY);
                if (now > was) {
                    failures.add("%s/%s increased from %d to %d. Emit a domain event instead."
                            .formatted(surface, family, was, now));
                } else if (now < was) {
                    failures.add("%s/%s dropped from %d to %d. Lower the ratchet baseline and "
                            .formatted(surface, family, was, now)
                            + "the matching DOMAIN_EVENTS.md ledger count.");
                }
            }
        }

        assertThat(failures)
                .withFailMessage(() -> "Legacy notification ratchet failed:\n  "
                        + String.join("\n  ", failures))
                .isEmpty();
    }

    private static Map<LegacySurface, Map<String, Integer>> scan(Path serviceRoot) throws IOException {
        Map<LegacySurface, Map<String, Integer>> counts = new EnumMap<>(LegacySurface.class);
        for (LegacySurface surface : LegacySurface.values()) {
            counts.put(surface, new TreeMap<>());
        }

        try (Stream<Path> paths = Files.walk(serviceRoot)) {
            for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .sorted()::iterator) {
                String source = Files.readString(path, StandardCharsets.UTF_8);
                String family = family(serviceRoot.relativize(path));

                for (LegacySurface surface : LegacySurface.values()) {
                    int count = count(source, surface.pattern);
                    if (path.getFileName().toString().equals("GameBroadcastService.java")
                            && surface.declarationInGameBroadcastService) {
                        count--;
                    }
                    if (count > 0) {
                        counts.get(surface).merge(family, count, Integer::sum);
                    }
                }
            }
        }
        return counts;
    }

    private static int count(String source, Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static String family(Path relativeFile) {
        Path parent = relativeFile.getParent();
        return parent == null ? ROOT_FAMILY : parent.toString().replace('\\', '/');
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

    private enum LegacySurface {
        BROADCAST_GAME_STATE(
                Pattern.compile("\\b(?:gameBroadcastService\\.)?broadcastGameState\\s*\\("),
                true),
        SESSION_SEND(
                Pattern.compile("\\bsessionManager\\.sendToPlayers?\\s*\\("),
                false),
        LOG_AND_BROADCAST(
                Pattern.compile("\\b(?:gameBroadcastService\\.)?logAndBroadcast\\s*\\("),
                true);

        private final Pattern pattern;
        private final boolean declarationInGameBroadcastService;

        LegacySurface(Pattern pattern, boolean declarationInGameBroadcastService) {
            this.pattern = pattern;
            this.declarationInGameBroadcastService = declarationInGameBroadcastService;
        }
    }
}
