package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGainKeywordsOfTriggeringCreatureEffect;

@CardRegistration(set = "YEOE", collectorNumber = "20")
public class MutablePupa extends Card {

    public MutablePupa() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PerpetuallyGainKeywordsOfTriggeringCreatureEffect());
    }
}
