package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "38")
public class LoomingHoverguard extends Card {

    public LoomingHoverguard() {
        target(TargetFilters.artifact())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new PutTargetOnTopOfLibraryEffect());
    }
}
