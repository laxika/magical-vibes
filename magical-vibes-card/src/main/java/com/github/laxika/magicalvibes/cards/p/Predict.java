package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NameCardMillTargetDrawEffect;

@CardRegistration(set = "ODY", collectorNumber = "94")
public class Predict extends Card {

    public Predict() {
        addEffect(EffectSlot.SPELL, new NameCardMillTargetDrawEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
