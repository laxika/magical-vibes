package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for "whenever you cast a spell matching the filter, creatures you control
 * get +1/+0 until end of turn for each color of mana spent to cast that spell".
 *
 * <p>The spell-cast trigger collector snapshots the distinct colored mana payment and turns this
 * descriptor into a regular {@link BoostAllOwnCreaturesEffect} with a fixed power bonus.</p>
 *
 * @param spellFilter which spells trigger this ({@code null} = any spell)
 */
public record BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect(
        CardPredicate spellFilter
) implements CardEffect {
}
