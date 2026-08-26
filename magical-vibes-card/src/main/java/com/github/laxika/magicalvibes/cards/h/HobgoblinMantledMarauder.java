package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "SPM", collectorNumber = "80")
public class HobgoblinMantledMarauder extends Card {

    public HobgoblinMantledMarauder() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new BoostSelfEffect(2, 0));
    }
}
