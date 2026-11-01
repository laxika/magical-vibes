package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentSharingTypeWithEnteringPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "257")
@CardRegistration(set = "MPS", collectorNumber = "10")
@CardRegistration(set = "RVR", collectorNumber = "255")
@CardRegistration(set = "LTC", collectorNumber = "349")
@CardRegistration(set = "LTC", collectorNumber = "379")
public class CloudstoneCurio extends Card {

    public CloudstoneCurio() {
        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                        new MayEffect(
                                new ReturnPermanentSharingTypeWithEnteringPermanentToHandEffect(),
                                "Return another permanent you control that shares a permanent type with it to its owner's hand?"
                        )));
    }
}
