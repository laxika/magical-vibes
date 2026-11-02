package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "308")
@CardRegistration(set = "9ED", collectorNumber = "280")
@CardRegistration(set = "MRD", collectorNumber = "139")
@CardRegistration(set = "DDU", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "782")
@CardRegistration(set = "C15", collectorNumber = "209")
public class ViridianShaman extends Card {

    public ViridianShaman() {
        target(TargetFilters.artifact()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyTargetPermanentEffect());
    }
}
