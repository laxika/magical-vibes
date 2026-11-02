package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "45")
@CardRegistration(set = "ME2", collectorNumber = "64")
@CardRegistration(set = "VMA", collectorNumber = "92")
@CardRegistration(set = "MH2", collectorNumber = "268")
public class SeaDrake extends Card {

    public SeaDrake() {
        target(TargetFilters.landYouControl(), 2, 2)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnToHandEffect.target());
    }
}
