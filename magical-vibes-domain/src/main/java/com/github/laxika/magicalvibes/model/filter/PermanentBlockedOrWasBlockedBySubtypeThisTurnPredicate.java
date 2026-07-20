package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Matches a creature that blocked or was blocked by a creature of the given subtype at any point this
 * turn (evaluated against the turn-scoped combat-block tracking recorded at declare-blockers time).
 * Whether the other creature was of the subtype is judged at the moment of the block, so the target
 * stays legal even after combat ends or the other creature leaves / changes types. Used by Time to
 * Reflect ("target creature that blocked or was blocked by a Zombie this turn").
 */
public record PermanentBlockedOrWasBlockedBySubtypeThisTurnPredicate(CardSubtype subtype) implements PermanentPredicate {
}
