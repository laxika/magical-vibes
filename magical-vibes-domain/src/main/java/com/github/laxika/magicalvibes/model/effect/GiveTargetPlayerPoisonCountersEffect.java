package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Give target player N poison counters.
 * <p>
 * When {@code spellFilter} is non-null, this doubles as a trigger descriptor for
 * {@code ON_ANY_PLAYER_CASTS_SPELL}: the trigger fires only when the controller casts
 * a spell matching the predicate. {@code checkSpellCastTriggers} resolves this into a
 * stack entry with a resolution-only copy ({@code spellFilter == null}).
 */
public record GiveTargetPlayerPoisonCountersEffect(int amount, CardPredicate spellFilter) implements CardEffect {

    /** Resolution-only constructor (no trigger filtering). */
    public GiveTargetPlayerPoisonCountersEffect(int amount) {
        this(amount, null);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
