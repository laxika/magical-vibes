package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "147")
public class WildCelebrants extends Card {

    public WildCelebrants() {
        // When this creature enters, you may destroy target artifact.
        target(TargetFilters.artifact()).addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new DestroyTargetPermanentEffect(),
                "Destroy target artifact?"
        ));
    }
}
