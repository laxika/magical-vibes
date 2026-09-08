package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Spell-cast trigger: target player reveals their hand and discards every card with the
 * triggering spell's mana value.
 *
 * @param spellFilter which spells trigger this
 */
public record DiscardAllCardsWithCastSpellManaValueEffect(CardPredicate spellFilter)
        implements CardEffect {
}
