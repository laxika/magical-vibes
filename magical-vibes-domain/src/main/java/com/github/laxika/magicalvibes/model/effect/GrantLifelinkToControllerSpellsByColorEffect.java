package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that grants lifelink to matching instant or sorcery spells controlled by this
 * permanent's controller. When {@code color} is non-null, only spells of that color qualify; a
 * null color grants lifelink to spells of every color. When {@code filter} is non-null, only cards
 * matching that predicate qualify. Checked when spell damage is dealt. Used by Firesong and
 * Sunspeaker, Radiant Scrollwielder, and Lo and Li, Twin Tutors.
 */
public record GrantLifelinkToControllerSpellsByColorEffect(CardColor color, CardPredicate filter)
        implements CardEffect {

    public GrantLifelinkToControllerSpellsByColorEffect(CardColor color) {
        this(color, null);
    }

    public static GrantLifelinkToControllerSpellsByColorEffect allColors() {
        return new GrantLifelinkToControllerSpellsByColorEffect(null, null);
    }

    public static GrantLifelinkToControllerSpellsByColorEffect forCardPredicate(CardPredicate filter) {
        return new GrantLifelinkToControllerSpellsByColorEffect(null, filter);
    }
}
