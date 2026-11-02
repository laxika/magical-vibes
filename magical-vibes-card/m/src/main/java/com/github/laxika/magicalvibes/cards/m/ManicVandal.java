package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "151")
@CardRegistration(set = "M12", collectorNumber = "151")
public class ManicVandal extends Card {

    public ManicVandal() {
        target(TargetFilters.artifact())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
