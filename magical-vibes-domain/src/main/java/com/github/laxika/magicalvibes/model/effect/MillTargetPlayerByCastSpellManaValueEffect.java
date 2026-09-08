package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Spell-cast trigger: target player mills cards equal to the triggering spell's mana value.
 *
 * @param spellFilter which spells trigger this
 */
public record MillTargetPlayerByCastSpellManaValueEffect(CardPredicate spellFilter) implements CardEffect {
}
