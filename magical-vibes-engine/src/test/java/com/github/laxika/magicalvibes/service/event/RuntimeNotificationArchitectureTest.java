package com.github.laxika.magicalvibes.service.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Permanent zero-tolerance invariants for runtime game notification architecture.
 */
class RuntimeNotificationArchitectureTest {

    @Test
    void removedNotificationFacadeAndMethodsCannotReturn() throws IOException {
        Path repoRoot = locateRepoRoot();
        String removedType = "Game" + "Broadcast" + "Service";
        String removedStateMethod = "broadcast" + "GameState";
        String removedLogMethod = "log" + "AndBroadcast";
        Pattern removedMethods = Pattern.compile(
                "\\b(?:" + removedStateMethod + "|" + removedLogMethod + ")\\s*\\(");
        List<String> violations = new ArrayList<>();

        for (Path path : productionJavaSources(repoRoot)) {
            String source = Files.readString(path, StandardCharsets.UTF_8);
            String relative = relative(repoRoot, path);
            if (source.contains(removedType)) {
                violations.add(relative + " references removed notification facade");
            }
            if (removedMethods.matcher(source).find()) {
                violations.add(relative + " defines or calls a removed notification method");
            }
        }

        assertThat(violations)
                .as("removed notification APIs must remain absent from production source")
                .isEmpty();
    }

    @Test
    void completedEventSubscribersCannotMutateAuthoritativeGameData() throws IOException {
        Path repoRoot = locateRepoRoot();
        List<String> violations = new ArrayList<>();
        Pattern monitorMutation = Pattern.compile("\\bsynchronized\\s*\\(\\s*gameData\\s*\\)");
        Pattern directAssignment = Pattern.compile(
                "\\bgameData\\.\\w+\\s*(?:=(?!=)|\\+=|-=|\\+\\+|--)");
        Pattern collectionMutation = Pattern.compile(
                "\\bgameData\\.\\w+\\.(?:add|addAll|put|putAll|remove|clear|set)\\s*\\(");

        for (Path path : productionJavaSources(repoRoot)) {
            String source = Files.readString(path, StandardCharsets.UTF_8);
            if (!source.contains("implements GameEventSubscriber")) {
                continue;
            }
            if (monitorMutation.matcher(source).find()) {
                violations.add(path.getFileName() + " acquires the GameData monitor");
            }
            if (directAssignment.matcher(source).find()
                    || collectionMutation.matcher(source).find()) {
                violations.add(path.getFileName() + " writes authoritative GameData");
            }
            if (source.contains("GameMutationCoordinator")) {
                violations.add(path.getFileName() + " starts or records another mutation");
            }
        }

        assertThat(violations)
                .as("completed event subscribers are observers, never authoritative mutators")
                .isEmpty();
    }

    private static List<Path> productionJavaSources(Path repoRoot) throws IOException {
        List<Path> sources = new ArrayList<>();
        try (Stream<Path> modules = Files.list(repoRoot)) {
            for (Path module : modules.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("magical-vibes-"))
                    .toList()) {
                Path main = module.resolve("src/main");
                if (Files.isDirectory(main)) {
                    sources.addAll(javaSources(main));
                }
            }
        }
        return sources;
    }

    private static List<Path> javaSources(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }

    private static String relative(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
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
