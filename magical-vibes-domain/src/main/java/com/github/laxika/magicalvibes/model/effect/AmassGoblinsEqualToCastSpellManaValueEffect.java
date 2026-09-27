package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Spell-cast trigger descriptor: amass the chosen creature type with a number of counters equal
 * to the triggering spell's full mana value.
 */
public record AmassGoblinsEqualToCastSpellManaValueEffect(CardSubtype subtype)
        implements CardEffect, TriggeringSpellManaValueEffect {

    public AmassGoblinsEqualToCastSpellManaValueEffect() {
        this(CardSubtype.GOBLIN);
    }

    @Override
    public CardEffect snapshotTriggeringSpellManaValue(int manaValue) {
        return new AmassGoblinsEffect(manaValue, subtype);
    }
}
