package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "261")
public class MirrorOfLifeTrapping extends Card {

    public MirrorOfLifeTrapping() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect());
    }
}
