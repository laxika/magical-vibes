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
    private static final Set<String> MIGRATED_LIFECYCLE_FILES = Set.of(
            "GameService.java",
            "GameSetupService.java",
            "MulliganService.java",
            "GameOutcomeService.java",
            "GameTimeoutService.java",
            "ReconnectionService.java",
            "StackResolutionService.java",
            "spell/SpellCastingService.java",
            "ability/AbilityActivationService.java",
            "ability/ActivatedAbilityExecutionService.java",
            "turn/AutoPassService.java",
            "turn/TurnProgressionService.java",
            "effect/normalfx/KarnRestartGameEffectHandler.java");
    private static final Set<String> COMBAT_INTERACTION_HANDLERS = Set.of(
            "interaction/AttackerDeclarationInteractionHandler.java",
            "interaction/BlockerDeclarationInteractionHandler.java",
            "interaction/CombatDamageAssignmentInteractionHandler.java");
    private static final Set<String> MIGRATED_STANDARD_INTERACTION_HANDLERS = Set.of(
            "interaction/AdNauseamRepeatChoiceInteractionHandler.java",
            "interaction/BrilliantUltimatumPlayChoiceInteractionHandler.java",
            "interaction/ColorChoiceInteractionHandler.java",
            "interaction/DoomsdayChoiceInteractionHandler.java",
            "interaction/GraveyardChoiceInteractionHandler.java",
            "interaction/GraveyardExileCostChoiceInteractionHandler.java",
            "interaction/HandCardChoiceInteractionHandlers.java",
            "interaction/HandTopBottomChoiceInteractionHandler.java",
            "interaction/IllicitAuctionBidChoiceInteractionHandler.java",
            "interaction/ImprovisationCapstoneCastChoiceInteractionHandler.java",
            "interaction/KeepCardsInHandChoiceInteractionHandler.java",
            "interaction/KnowledgePoolCastChoiceInteractionHandler.java",
            "interaction/LibraryReorderInteractionHandler.java",
            "interaction/LibraryRevealChoiceInteractionHandler.java",
            "interaction/LibrarySearchInteractionHandler.java",
            "interaction/MayAbilityChoiceInteractionHandler.java",
            "interaction/MirrorOfFateChoiceInteractionHandler.java",
            "interaction/MultiGraveyardChoiceInteractionHandler.java",
            "interaction/MultiPermanentChoiceInteractionHandler.java",
            "interaction/MultiZoneExileChoiceInteractionHandler.java",
            "interaction/PermanentAuctionChoiceInteractionHandler.java",
            "interaction/PermanentChoiceInteractionHandler.java",
            "interaction/PutCardsFromHandOnLibraryCardChoiceInteractionHandler.java",
            "interaction/PutCardsFromHandOnLibraryDestinationChoiceInteractionHandler.java",
            "interaction/RevealCardsDiscardChoiceInteractionHandler.java",
            "interaction/RevealedHandChoiceInteractionHandler.java",
            "interaction/ScryInteractionHandler.java",
            "interaction/SearchLibraryToTopChoiceInteractionHandler.java",
            "interaction/SylvanLibraryChoiceInteractionHandler.java",
            "interaction/XValueChoiceInteractionHandler.java");
    private static final Map<LegacySurface, Map<String, Integer>> BASELINE = new EnumMap<>(LegacySurface.class);

    static {
        BASELINE.put(LegacySurface.BROADCAST_GAME_STATE, Map.of(
                ROOT_FAMILY, 1,
                "effect/normalfx", 15,
                "interaction", 3));

        BASELINE.put(LegacySurface.SESSION_SEND, Map.of(
                ROOT_FAMILY, 1,
                "effect/normalfx", 7));

        BASELINE.put(LegacySurface.LOG_AND_BROADCAST, Map.ofEntries(
                Map.entry(ROOT_FAMILY, 111),
                Map.entry("ability", 40),
                Map.entry("ability/cost", 8),
                Map.entry("aura", 4),
                Map.entry("battle", 4),
                Map.entry("battlefield", 40),
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

    @Test
    void migratedLifecycleFilesHaveZeroDirectStateOrSessionDelivery() throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        List<String> failures = new ArrayList<>();

        for (String relative : MIGRATED_LIFECYCLE_FILES) {
            String source = Files.readString(serviceRoot.resolve(relative), StandardCharsets.UTF_8);
            for (LegacySurface surface : List.of(
                    LegacySurface.BROADCAST_GAME_STATE,
                    LegacySurface.SESSION_SEND)) {
                int current = count(source, surface.pattern);
                if (current != 0) {
                    failures.add(relative + " retains " + current + " " + surface + " call(s)");
                }
            }
        }

        assertThat(failures)
                .withFailMessage(() -> "Migrated lifecycle notification surface regressed:\n  "
                        + String.join("\n  ", failures))
                .isEmpty();
    }

    @Test
    void allInputFilesHaveZeroDirectStateOrSessionDelivery() throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        Path inputRoot = serviceRoot.resolve("input");
        List<String> failures = new ArrayList<>();

        try (Stream<Path> paths = Files.list(inputRoot)) {
            for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .sorted()::iterator) {
                String source = Files.readString(path, StandardCharsets.UTF_8);
                for (LegacySurface surface : List.of(
                        LegacySurface.BROADCAST_GAME_STATE,
                        LegacySurface.SESSION_SEND)) {
                    int current = count(source, surface.pattern);
                    if (current != 0) {
                        failures.add("input/" + path.getFileName() + " retains "
                                + current + " " + surface + " call(s)");
                    }
                }
            }
        }

        assertThat(failures)
                .withFailMessage(() -> "Input notification migration regressed:\n  "
                        + String.join("\n  ", failures))
                .isEmpty();
    }

    @Test
    void combatPackagesAndInteractionHandlersHaveNoLegacyNotificationSurface() throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        List<Path> sources = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(serviceRoot.resolve("combat"))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(sources::add);
        }
        COMBAT_INTERACTION_HANDLERS.stream()
                .map(serviceRoot::resolve)
                .forEach(sources::add);

        List<String> failures = new ArrayList<>();
        for (Path path : sources) {
            String source = Files.readString(path, StandardCharsets.UTF_8);
            for (LegacySurface surface : LegacySurface.values()) {
                int current = count(source, surface.pattern);
                if (current != 0) {
                    failures.add(serviceRoot.relativize(path) + " retains "
                            + current + " " + surface + " call(s)");
                }
            }
        }

        assertThat(failures)
                .withFailMessage(() -> "Combat notification migration regressed:\n  "
                        + String.join("\n  ", failures))
                .isEmpty();
    }

    @Test
    void migratedStandardInteractionHandlersHaveNoSessionOrNetworkingProjectionDependency()
            throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        List<String> failures = new ArrayList<>();

        for (String relative : MIGRATED_STANDARD_INTERACTION_HANDLERS) {
            String source = Files.readString(serviceRoot.resolve(relative), StandardCharsets.UTF_8);
            int sessionSends = count(source, LegacySurface.SESSION_SEND.pattern);
            if (sessionSends != 0) {
                failures.add(relative + " retains " + sessionSends + " direct session send(s)");
            }
            if (source.contains("import com.github.laxika.magicalvibes.networking.")) {
                failures.add(relative + " retains a networking projection dependency");
            }
        }

        assertThat(failures)
                .withFailMessage(() -> "Standard interaction prompt migration regressed:\n  "
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
