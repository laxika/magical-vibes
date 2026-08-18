package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Triggered ability: whenever the controller casts a spell matching the filter,
 * destroy all permanents with that spell's mana value.
 *
 * @param spellFilter which spells trigger this ability
 */
public record DestroyAllPermanentsWithCastSpellManaValueEffect(CardPredicate spellFilter)
        implements CardEffect {
}
