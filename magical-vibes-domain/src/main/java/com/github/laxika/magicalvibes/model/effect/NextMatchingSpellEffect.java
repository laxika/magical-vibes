package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * An effect that waits for "the next matching spell" its player casts and ends with that cast
 * ({@link Duration.End#MATCHING_SPELL_CAST}): a cost change, a flash, convoke, free-cast or life-payment grant, a
 * can't-be-countered or empowerment rider. The duration clock ends it as the matching spell is cast, or at the cleanup
 * step when it {@linkplain #expiresAtCleanup() lasts only the turn}; one that is used up at another moment (the free
 * cast permission as its cost is paid) ends then as well.
 */
public interface NextMatchingSpellEffect extends CardEffect {

    /** The spell the effect waits for: the first spell cast that matches it ends the effect. */
    CardPredicate predicate();

    /** Whether the spell cast by {@code castingPlayerId} is one the effect waits for. */
    default boolean appliesToPlayer(UUID castingPlayerId, UUID effectControllerId) {
        return effectControllerId != null && effectControllerId.equals(castingPlayerId);
    }

    /** Whether a spell cast face down (or not) is one the effect waits for. */
    default boolean appliesToFaceDownCast(boolean castFaceDown) {
        return true;
    }

    /** Whether the effect wears off at the cleanup step if no matching spell was cast this turn. */
    default boolean expiresAtCleanup() {
        return true;
    }
}
