package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "ISD", collectorNumber = "86")
public class AltarsReap extends Card {

    public AltarsReap() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
