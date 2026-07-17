package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Triggered ability: whenever controller casts a spell matching the filter,
 * this permanent gets +X/+X until end of turn, where X is that spell's mana value.
 * <p>
 * Used in {@code ON_CONTROLLER_CASTS_SPELL} slot. The trigger handler reads the
 * cast spell's mana value and queues a self-targeting {@link BoostSelfEffect}.
 * A {@code null} filter matches every spell (Manaplasm).
 *
 * @param spellFilter which spells trigger this ({@code null} = any spell)
 */
public record BoostSelfByCastSpellManaValueEffect(
        CardPredicate spellFilter
) implements CardEffect {
}
