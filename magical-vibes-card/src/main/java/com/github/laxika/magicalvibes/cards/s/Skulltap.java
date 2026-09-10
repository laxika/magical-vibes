package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "SCG", collectorNumber = "73")
public class Skulltap extends Card {

    public Skulltap() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
