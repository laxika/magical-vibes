package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Spell-cast trigger: the controller gains life equal to the triggering spell's mana value.
 *
 * @param spellFilter which spells trigger this
 */
public record GainLifeEqualToCastSpellManaValueEffect(CardPredicate spellFilter) implements CardEffect {
}
