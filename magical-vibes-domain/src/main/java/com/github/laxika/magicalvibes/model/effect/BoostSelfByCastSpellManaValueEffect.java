package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Triggered ability: whenever controller casts a spell matching the filter,
 * this permanent gets +X/+X until end of turn, where X is that spell's mana value. When
 * {@code boostToughness} is false, the boost is +X/+0 instead.
 * <p>
 * Used in {@code ON_CONTROLLER_CASTS_SPELL} slot. The trigger handler reads the
 * cast spell's mana value and queues a self-targeting {@link BoostSelfEffect}.
 * A {@code null} filter matches every spell (Manaplasm).
 *
 * @param spellFilter which spells trigger this ({@code null} = any spell)
 * @param boostToughness whether the mana-value boost also applies to toughness
 */
public record BoostSelfByCastSpellManaValueEffect(
        CardPredicate spellFilter,
        boolean boostToughness
) implements CardEffect {

    public BoostSelfByCastSpellManaValueEffect(CardPredicate spellFilter) {
        this(spellFilter, true);
    }
}
