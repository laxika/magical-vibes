package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect;

@CardRegistration(set = "HBG", collectorNumber = "261")
public class MirrorOfLifeTrapping extends Card {

    public MirrorOfLifeTrapping() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect());
    }
}
