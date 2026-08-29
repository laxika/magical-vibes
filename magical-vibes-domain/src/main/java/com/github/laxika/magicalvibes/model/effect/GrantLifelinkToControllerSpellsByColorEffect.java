package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Static effect that grants lifelink to instant or sorcery spells controlled by this permanent's
 * controller. With a color, only spells of that color qualify; the all-spell-colors factory matches
 * spells of every color. Checked when spell damage is dealt. Used by Firesong and Sunspeaker and
 * Soulfire Grand Master.
 */
public record GrantLifelinkToControllerSpellsByColorEffect(CardColor color, boolean allColors) implements CardEffect {

    public GrantLifelinkToControllerSpellsByColorEffect(CardColor color) {
        this(color, false);
    }

    public static GrantLifelinkToControllerSpellsByColorEffect allSpellColors() {
        return new GrantLifelinkToControllerSpellsByColorEffect(null, true);
    }
}
