package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Conditional combat-trigger effect: "Whenever this creature blocks or becomes blocked by one or more
 * creatures matching {@code opponentFilter}, this creature gets +X/+Y (and optionally gains
 * {@code grantedKeywords}) until end of turn."
 * <p>
 * Place in the {@code ON_BLOCK} and/or {@code ON_BECOMES_BLOCKED} slot. The trigger is pushed onto the
 * stack unconditionally (non-targeting, source = this creature); at resolution the handler inspects this
 * creature's combat opponents (creatures it blocks + creatures blocking it) and applies the boost only if
 * at least one matches {@code opponentFilter}. Fires once regardless of how many opponents match, matching
 * the "one or more" wording. Used by Dwarven Soldier ({@code PermanentHasSubtypePredicate(ORC)}, +0/+2)
 * and Crimson Roc (non-flying opponent, +1/+0 plus first strike).
 *
 * @param grantedKeywords keywords the source also gains until end of turn; empty for a pure boost
 */
public record BoostSelfWhenCombatOpponentMatchesEffect(PermanentPredicate opponentFilter,
                                                       int powerBoost,
                                                       int toughnessBoost,
                                                       Set<Keyword> grantedKeywords) implements CardEffect {

    public BoostSelfWhenCombatOpponentMatchesEffect(PermanentPredicate opponentFilter, int powerBoost, int toughnessBoost) {
        this(opponentFilter, powerBoost, toughnessBoost, Set.of());
    }
}
