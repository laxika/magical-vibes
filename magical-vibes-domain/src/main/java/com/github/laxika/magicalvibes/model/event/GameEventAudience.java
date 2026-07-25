package com.github.laxika.magicalvibes.model.event;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Explicit visibility and audience for a domain event.
 *
 * <p>The safe default is {@link #internalOnly()}. Callers must opt in to any player-visible
 * audience; there is no factory whose omitted arguments mean "everyone".
 */
public record GameEventAudience(Visibility visibility, Set<UUID> playerIds) {

    public GameEventAudience {
        Objects.requireNonNull(visibility, "visibility");
        playerIds = Set.copyOf(Objects.requireNonNull(playerIds, "playerIds"));

        if (visibility == Visibility.PRIVATE && playerIds.isEmpty()) {
            throw new IllegalArgumentException("A private event must name at least one recipient");
        }
        if (visibility != Visibility.PRIVATE && !playerIds.isEmpty()) {
            throw new IllegalArgumentException(
                    visibility + " events cannot carry a player-specific recipient list");
        }
    }

    public static GameEventAudience internalOnly() {
        return new GameEventAudience(Visibility.INTERNAL, Set.of());
    }

    public static GameEventAudience allPlayers() {
        return new GameEventAudience(Visibility.PUBLIC, Set.of());
    }

    public static GameEventAudience player(UUID playerId) {
        return players(playerId);
    }

    public static GameEventAudience players(UUID... playerIds) {
        Objects.requireNonNull(playerIds, "playerIds");
        return new GameEventAudience(Visibility.PRIVATE, Set.copyOf(Arrays.asList(playerIds)));
    }

    public boolean isVisibleTo(UUID playerId) {
        return switch (visibility) {
            case INTERNAL -> false;
            case PUBLIC -> true;
            case PRIVATE -> playerIds.contains(playerId);
        };
    }

    public enum Visibility {
        INTERNAL,
        PUBLIC,
        PRIVATE
    }
}
