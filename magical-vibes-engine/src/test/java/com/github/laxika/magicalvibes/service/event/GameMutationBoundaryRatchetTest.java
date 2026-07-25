package com.github.laxika.magicalvibes.service.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prevents new externally callable game mutations from restoring ad-hoc monitor ownership.
 *
 * <p>The engine intentionally has many public helper methods for handler dispatch and focused
 * tests. The canonical runtime boundaries are narrower: GameService commands, setup/join, and
 * timeout callbacks. Lower-level tests that need event recording use
 * {@link GameMutationCoordinator#mutate(com.github.laxika.magicalvibes.model.GameData, Runnable)}
 * explicitly.
 */
class GameMutationBoundaryRatchetTest {

    private static final Pattern PUBLIC_METHOD = Pattern.compile(
            "\\bpublic\\s+([\\w<>?,.\\[\\] ]+)\\s+(\\w+)\\s*\\((.*?)\\)\\s*\\{",
            Pattern.DOTALL);

    @Test
    void canonicalMutationEntryPointsCannotBypassTheCoordinator() throws IOException {
        Path root = locateRepoRoot();
        String gameService = source(root,
                "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/GameService.java");
        String setupService = source(root,
                "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/GameSetupService.java");
        String timeoutService = source(root,
                "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/GameTimeoutService.java");
        String coordinator = source(root,
                "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/event/GameMutationCoordinator.java");
        String simulator = source(root,
                "magical-vibes-ai/src/main/java/com/github/laxika/magicalvibes/ai/simulation/GameSimulator.java");
        String aiPlayerService = source(root,
                "magical-vibes-ai/src/main/java/com/github/laxika/magicalvibes/ai/AiPlayerService.java");
        String draftService = source(root,
                "magical-vibes-webservice/src/main/java/com/github/laxika/magicalvibes/webservice/DraftService.java");

        List<String> bypasses = new ArrayList<>();
        for (MethodSource method : publicMethods(gameService)) {
            if (method.returnType().trim().equals("void") && method.parameters().contains("GameData")
                    && !joinsCoordinatorOrDelegates(method)) {
                bypasses.add("GameService." + method.name());
            }
        }
        for (MethodSource method : publicMethods(setupService)) {
            if ((method.name().equals("createGame") || method.name().equals("joinGame"))
                    && !joinsCoordinatorOrDelegates(method)) {
                bypasses.add("GameSetupService." + method.name());
            }
        }
        for (String callback : List.of(
                "onPlayerDisconnect", "onPlayerReconnect", "singleGoneTimerFired", "bothGoneTimerFired")) {
            MethodSource method = method(timeoutService, callback);
            if (!method.body().contains("mutationCoordinator")) {
                bypasses.add("GameTimeoutService." + callback);
            }
        }
        for (MethodSource method : publicMethods(aiPlayerService)) {
            if (method.name().equals("joinAsAi") && !joinsCoordinatorOrDelegates(method)) {
                bypasses.add("AiPlayerService." + method.name());
            }
        }
        if (!method(draftService, "createDraftGame").body().contains("mutationCoordinator.mutate")) {
            bypasses.add("DraftService.createDraftGame");
        }

        assertThat(bypasses)
                .withFailMessage(() -> "GameData mutation entry points bypass the coordinator: " + bypasses)
                .isEmpty();
        assertThat(coordinator).doesNotContain("ThreadLocal");
        assertThat(simulator).doesNotContain("synchronized (gd)");
    }

    private static boolean joinsCoordinatorOrDelegates(MethodSource method) {
        return method.body().contains("runAsActionIfNeeded")
                || method.body().contains("mutationCoordinator.mutate")
                || Pattern.compile("\\b" + Pattern.quote(method.name()) + "\\s*\\(")
                        .matcher(method.body()).find();
    }

    private static List<MethodSource> publicMethods(String source) {
        List<MethodSource> methods = new ArrayList<>();
        Matcher matcher = PUBLIC_METHOD.matcher(source);
        while (matcher.find()) {
            methods.add(new MethodSource(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3),
                    body(source, matcher.end() - 1)));
        }
        return methods;
    }

    private static MethodSource method(String source, String name) {
        Pattern declaration = Pattern.compile(
                "\\b(?:public|private|protected)\\s+[\\w<>?,.\\[\\] ]+\\s+"
                        + Pattern.quote(name) + "\\s*\\((.*?)\\)\\s*\\{",
                Pattern.DOTALL);
        Matcher matcher = declaration.matcher(source);
        if (!matcher.find()) {
            throw new AssertionError("Missing method " + name);
        }
        return new MethodSource("", name, matcher.group(1), body(source, matcher.end() - 1));
    }

    private static String body(String source, int openingBrace) {
        int depth = 0;
        for (int i = openingBrace; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                return source.substring(openingBrace + 1, i);
            }
        }
        throw new AssertionError("Unbalanced method body");
    }

    private static String source(Path root, String relative) throws IOException {
        return Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
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

    private record MethodSource(String returnType, String name, String parameters, String body) {
    }
}
