package com.github.laxika.magicalvibes.model.rules;

import java.util.Objects;
import java.util.UUID;

/**
 * A player searched their library, or a player's library was shuffled: the events "whenever a player searches their
 * library" and "whenever a player shuffles their library" abilities trigger on (CR 603.2).
 *
 * @param kind                whether the library was searched or shuffled
 * @param playerId            the player who searched their library, or whose library was shuffled
 * @param causingControllerId the controller of the spell or ability that caused the shuffle, or {@code null} when
 *                            none was resolving
 */
public record LibraryEvent(Kind kind, UUID playerId, UUID causingControllerId) implements RulesEvent {

    public LibraryEvent {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(playerId, "playerId");
    }

    /** {@code playerId} searched their library. */
    public static LibraryEvent searched(UUID playerId) {
        return new LibraryEvent(Kind.SEARCHED, playerId, null);
    }

    /** {@code playerId}'s library was shuffled, by a spell or ability {@code causingControllerId} controls. */
    public static LibraryEvent shuffled(UUID playerId, UUID causingControllerId) {
        return new LibraryEvent(Kind.SHUFFLED, playerId, causingControllerId);
    }

    @Override
    public LibraryEvent deepCopy() {
        return this;
    }

    /** What happened to the library. */
    public enum Kind {
        /** Its owner searched it. */
        SEARCHED,
        /** It was shuffled. */
        SHUFFLED
    }
}
