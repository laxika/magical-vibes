package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "OTJ", collectorNumber = "84")
public class CorruptedConviction extends Card {

    public CorruptedConviction() {
        // As an additional cost to cast this spell, sacrifice a creature.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
