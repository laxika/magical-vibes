package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "230")
public class MedicineRunner extends Card {

    public MedicineRunner() {
        // When this creature enters, you may remove a counter from target permanent.
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(new RemoveCounterFromTargetPermanentEffect(),
                                "Remove a counter from target permanent?"));
    }
}
