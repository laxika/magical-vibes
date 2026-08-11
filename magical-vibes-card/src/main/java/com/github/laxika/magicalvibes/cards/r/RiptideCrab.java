package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "INV", collectorNumber = "266")
public class RiptideCrab extends Card {

    public RiptideCrab() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
