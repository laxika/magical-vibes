package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Spell-cast trigger: return up to one target nonland permanent an opponent controls to its
 * owner's hand when the controller casts a spell whose mana value is at least the permanent's.
 *
 * @param spellFilter which spells trigger this ({@code null} = any spell)
 */
public record ReturnTargetPermanentByCastSpellManaValueEffect(CardPredicate spellFilter)
        implements CardEffect {

    public ReturnTargetPermanentByCastSpellManaValueEffect() {
        this(null);
    }
}
