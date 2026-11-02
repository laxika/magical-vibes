package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "S99", collectorNumber = "106")
@CardRegistration(set = "VMA", collectorNumber = "172")
@CardRegistration(set = "SLD", collectorNumber = "928")
@CardRegistration(set = "SLD", collectorNumber = "1076")
@CardRegistration(set = "MB2", collectorNumber = "131")
public class GoblinSettler extends Card {

    public GoblinSettler() {
        // When this creature enters, destroy target land.
        target(TargetFilters.land()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyTargetPermanentEffect());
    }
}
