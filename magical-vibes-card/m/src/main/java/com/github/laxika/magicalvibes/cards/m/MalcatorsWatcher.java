package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ONE", collectorNumber = "58")
public class MalcatorsWatcher extends Card {

    public MalcatorsWatcher() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
