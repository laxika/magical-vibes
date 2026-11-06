package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentWasCastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "261")
public class MirrorOfLifeTrapping extends Card {

    public MirrorOfLifeTrapping() {
        // "Whenever a creature enters, if it was cast": whether it was cast can't change, so it is the filter.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentWasCastPredicate())),
                        ObjectRelation.ANOTHER),
                new ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect());
    }
}
