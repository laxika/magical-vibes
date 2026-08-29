package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Spell-cast trigger descriptor for a permanent that becomes an Illusion creature whose base
 * power and toughness each equal the mana value of the triggering spell.
 *
 * @param spellFilter which spells trigger this ({@code null} = any spell)
 */
public record BecomeCreatureByCastSpellManaValueEffect(CardPredicate spellFilter) implements CardEffect {

    public BecomeCreatureByCastSpellManaValueEffect() {
        this(null);
    }
}
