package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "BFZ", collectorNumber = "66")
public class SpellShrivel extends Card {

    public SpellShrivel() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(4, false, true));
    }
}
