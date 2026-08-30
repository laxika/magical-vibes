package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: the players selected by {@code scope} can't cast more than {@code maxSpells}
 * spells each turn. Enforced in {@code CastingPermissionService.isSpellLimitReached}; when several
 * limits apply to the same player the most restrictive (lowest) value wins. When
 * {@code spellFilter} is non-null, only matching spells count toward this limit.
 */
public record LimitSpellsPerTurnEffect(int maxSpells, SpellLimitScope scope, CardPredicate spellFilter)
        implements CardEffect {

    public LimitSpellsPerTurnEffect(int maxSpells, SpellLimitScope scope) {
        this(maxSpells, scope, null);
    }
}
