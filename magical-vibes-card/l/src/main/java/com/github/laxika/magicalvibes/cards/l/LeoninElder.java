package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "10")
public class LeoninElder extends Card {

    public LeoninElder() {
        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(),
                        new MayEffect(new GainLifeEffect(1), "Gain 1 life?"),
                        true));
    }
}
