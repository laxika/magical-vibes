package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;


@CardRegistration(set = "ALA", collectorNumber = "57")
public class SpellSnip extends Card {

    public SpellSnip() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
