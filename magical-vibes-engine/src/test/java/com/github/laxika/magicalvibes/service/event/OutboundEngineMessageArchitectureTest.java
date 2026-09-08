package com.github.laxika.magicalvibes.service.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps engine output one-way: mutation services emit facts and projectors construct messages.
 *
 * <p>The session adapter allowlist is intentionally tiny. {@code GameMessageTransport} owns typed
 * delivery and {@code GameSessionTransportAdapter} exposes read-only connection state.
 * Reconnect projections use those adapters and the canonical prompt projector, never raw sessions.
 */
class OutboundEngineMessageArchitectureTest {

    private static final String SERVICE_ROOT =
            "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service";
    private static final String NETWORK_MESSAGE_ROOT =
            "magical-vibes-networking/src/main/java/com/github/laxika/magicalvibes/networking/message";

    private static final Set<String> NON_OUTBOUND_WIRE_TYPES = Set.of(
            "BlockerAssignment");

    private static final Set<String> SESSION_ADAPTERS = Set.of(
            "GameMessageTransport.java",
            "GameSessionTransportAdapter.java");

    private static final Set<String> MESSAGE_TRANSPORT_BOUNDARIES = Set.of(
            "GameResyncProjectionService.java",
            "ReconnectionService.java",
            "event/GameEventProjectionSubscriber.java");
    private static final Set<String> PROJECTION_AND_TRANSPORT_OWNERS = Set.of(
            "GameMessageTransport.java",
            "GameSessionTransportAdapter.java",
            "GameResyncProjectionService.java",
            "GameViewProjectionFactory.java",
            "PrivateInformationProjectionFactory.java",
            "ReconnectionService.java",
            "event/GameEndLifecycleSubscriber.java",
            "event/GameEventProjectionSubscriber.java",
            "event/InteractionPromptProjectionRegistry.java");
    private static final Set<String> PROJECTION_AND_TRANSPORT_TYPES = Set.of(
            "GameMessageTransport",
            "GameSessionTransportAdapter",
            "GameResyncProjectionService",
            "GameViewProjectionFactory",
            "PrivateInformationProjectionFactory",
            "InteractionPromptProjectionRegistry",
            "ReconnectionService");

    private static final Map<String, Set<String>> MESSAGE_PROJECTORS = messageProjectors();

    @Test
    void onlyExplicitTransportAdaptersDependOnSessionManager() throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        List<String> violations = new ArrayList<>();

        for (Path path : javaSources(serviceRoot)) {
            String source = Files.readString(path, StandardCharsets.UTF_8);
            String relative = relative(serviceRoot, path);
            boolean dependsOnSessionManager = source.contains(
                    "import com.github.laxika.magicalvibes.networking.SessionManager;");
            if (dependsOnSessionManager && !SESSION_ADAPTERS.contains(relative)) {
                violations.add(relative + " depends on SessionManager");
            }
            if (Pattern.compile("\\bsessionManager\\.sendToPlayers?\\s*\\(")
                    .matcher(source).find()
                    && !relative.equals("GameMessageTransport.java")) {
                violations.add(relative + " sends directly through SessionManager");
            }
            if (Pattern.compile("\\.sendMessage\\s*\\(").matcher(source).find()) {
                violations.add(relative + " sends directly through Connection");
            }
        }

        assertThat(violations)
                .as("SessionManager is restricted to the documented transport adapter allowlist")
                .isEmpty();
    }

    @Test
    void onlyProjectionAndReconnectBoundariesCanDeliverTypedMessages() throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        List<String> violations = new ArrayList<>();
        Pattern transportDependency = Pattern.compile("\\bGameMessageTransport\\s+\\w+");
        Pattern transportConstruction = Pattern.compile("\\bnew\\s+GameMessageTransport\\s*\\(");

        for (Path path : javaSources(serviceRoot)) {
            String source = Files.readString(path, StandardCharsets.UTF_8);
            String relative = relative(serviceRoot, path);
            if (relative.equals("GameMessageTransport.java")) {
                continue;
            }
            if ((transportDependency.matcher(source).find()
                    || transportConstruction.matcher(source).find())
                    && !MESSAGE_TRANSPORT_BOUNDARIES.contains(relative)) {
                violations.add(relative + " depends on typed message transport");
            }
        }

        assertThat(violations)
                .as("typed delivery is restricted to event projections and reconnect boundaries")
                .isEmpty();
    }

    @Test
    void mutationAndRulesServicesCannotDependOnProjectionOrTransportServices()
            throws IOException {
        Path serviceRoot = locateRepoRoot().resolve(SERVICE_ROOT);
        List<String> violations = new ArrayList<>();

        for (Path path : javaSources(serviceRoot)) {
            String relative = relative(serviceRoot, path);
            if (PROJECTION_AND_TRANSPORT_OWNERS.contains(relative)) {
                continue;
            }
            String source = Files.readString(path, StandardCharsets.UTF_8);
            for (String forbiddenType : PROJECTION_AND_TRANSPORT_TYPES) {
                if (Pattern.compile("\\b" + Pattern.quote(forbiddenType) + "\\b")
                        .matcher(source)
                        .find()) {
                    violations.add(relative + " depends on " + forbiddenType);
                }
            }
        }

        assertThat(violations)
                .as("mutation and rules services must stay independent of projection/transport")
                .isEmpty();
    }

    @Test
    void outboundGameMessagesAreConstructedOnlyByTheirProjectors() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path serviceRoot = repoRoot.resolve(SERVICE_ROOT);
        Path messageRoot = repoRoot.resolve(NETWORK_MESSAGE_ROOT);
        List<String> violations = new ArrayList<>();

        for (Path path : javaSources(serviceRoot)) {
            String source = Files.readString(path, StandardCharsets.UTF_8);
            String relative = relative(serviceRoot, path);
            for (String messageType : wireTypes(messageRoot)) {
                if (NON_OUTBOUND_WIRE_TYPES.contains(messageType)) {
                    continue;
                }
                Pattern construction = messageType.equals("InteractionPromptMessage")
                        ? Pattern.compile("\\bInteractionPromptMessage\\s*\\.")
                        : Pattern.compile("\\bnew\\s+" + Pattern.quote(messageType) + "\\s*\\(");
                if (!construction.matcher(source).find()) {
                    continue;
                }
                Set<String> allowed = MESSAGE_PROJECTORS.get(messageType);
                if (allowed == null) {
                    violations.add(relative + " constructs unclassified wire type " + messageType);
                } else if (!allowed.contains(relative)) {
                    violations.add(relative + " constructs " + messageType);
                }
            }
        }

        assertThat(violations)
                .as("outbound engine messages must be constructed by canonical projectors")
                .isEmpty();
    }

    private static Map<String, Set<String>> messageProjectors() {
        Map<String, Set<String>> projectors = new LinkedHashMap<>();
        projectors.put("AttackTarget",
                Set.of("event/InteractionPromptProjectionRegistry.java"));
        projectors.put("GameStateMessage", Set.of("GameViewProjectionFactory.java"));
        projectors.put("JoinGame", Set.of("GameViewProjectionFactory.java"));
        projectors.put("JoinGameMessage", Set.of("GameResyncProjectionService.java"));
        projectors.put("InteractionPromptMessage",
                Set.of("event/InteractionPromptProjectionRegistry.java"));
        projectors.put("AvailableAttackersMessage",
                Set.of("event/InteractionPromptProjectionRegistry.java"));
        projectors.put("AvailableBlockersMessage",
                Set.of("event/InteractionPromptProjectionRegistry.java"));
        projectors.put("CombatDamageAssignmentNotification",
                Set.of("event/InteractionPromptProjectionRegistry.java"));
        projectors.put("SelectCardsToBottomMessage",
                Set.of("event/InteractionPromptProjectionRegistry.java"));
        projectors.put("MulliganResolvedMessage",
                Set.of("event/GameEventProjectionSubscriber.java"));
        projectors.put("RevealHandMessage", Set.of("PrivateInformationProjectionFactory.java"));
        projectors.put("RevealLibraryTopMessage",
                Set.of("PrivateInformationProjectionFactory.java"));
        projectors.put("RevealPermanentMessage",
                Set.of("PrivateInformationProjectionFactory.java"));
        projectors.put("GameOverMessage",
                Set.of("event/GameEventProjectionSubscriber.java"));
        // Read-only request/response query projection; never used for mutation notifications.
        projectors.put("ValidTargetsResponse", Set.of("target/ValidTargetService.java"));
        return Map.copyOf(projectors);
    }

    private static List<String> wireTypes(Path messageRoot) throws IOException {
        try (Stream<Path> paths = Files.list(messageRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.substring(0, fileName.length() - ".java".length());
                    })
                    .sorted()
                    .toList();
        }
    }

    private static List<Path> javaSources(Path serviceRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(serviceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }

    private static String relative(Path serviceRoot, Path path) {
        return serviceRoot.relativize(path).toString().replace('\\', '/');
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
