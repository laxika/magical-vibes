package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional combat-trigger effect: "Whenever this creature blocks or becomes blocked by one or more
 * creatures matching {@code opponentFilter}, this creature gets +X/+Y until end of turn."
 * <p>
 * Place in the {@code ON_BLOCK} and/or {@code ON_BECOMES_BLOCKED} slot. The trigger is pushed onto the
 * stack unconditionally (non-targeting, source = this creature); at resolution the handler inspects this
 * creature's combat opponents (creatures it blocks + creatures blocking it) and applies the boost only if
 * at least one matches {@code opponentFilter}. Fires once regardless of how many opponents match, matching
 * the "one or more" wording. Used by Dwarven Soldier ({@code PermanentHasSubtypePredicate(ORC)}, +0/+2).
 */
public record BoostSelfWhenCombatOpponentMatchesEffect(PermanentPredicate opponentFilter,
                                                       int powerBoost,
                                                       int toughnessBoost) implements CardEffect {
}
