package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "87")
public class Batterhorn extends Card {

    public Batterhorn() {
        target(TargetFilters.artifact())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target artifact?"));
    }
}
