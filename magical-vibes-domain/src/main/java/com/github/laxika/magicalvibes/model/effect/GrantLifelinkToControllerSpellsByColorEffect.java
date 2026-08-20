package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Static effect that grants lifelink to instant or sorcery spells controlled by this permanent's
 * controller. When {@code color} is non-null, only spells of that color qualify; a null color
 * grants lifelink to spells of every color. Checked when spell damage is dealt. Used by Firesong
 * and Sunspeaker and Radiant Scrollwielder.
 */
public record GrantLifelinkToControllerSpellsByColorEffect(CardColor color) implements CardEffect {

    public static GrantLifelinkToControllerSpellsByColorEffect allColors() {
        return new GrantLifelinkToControllerSpellsByColorEffect(null);
    }
}
