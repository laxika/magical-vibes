package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "INV", collectorNumber = "266")
@CardRegistration(set = "MB1", collectorNumber = "228")
public class RiptideCrab extends Card {

    public RiptideCrab() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
