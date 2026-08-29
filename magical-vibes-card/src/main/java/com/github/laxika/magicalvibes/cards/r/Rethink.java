package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "PCY", collectorNumber = "42")
public class Rethink extends Card {

    public Rethink() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(new TargetSpellManaValue()));
    }
}
