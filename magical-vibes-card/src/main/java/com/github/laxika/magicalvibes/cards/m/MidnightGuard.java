package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;

@CardRegistration(set = "DKA", collectorNumber = "14")
public class MidnightGuard extends Card {

    public MidnightGuard() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new UntapSelfEffect());
    }
}
