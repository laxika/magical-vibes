package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "76")
public class ReplicationSpecialist extends Card {

    public ReplicationSpecialist() {
        addTrigger(Triggers.enters(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_CONTROL), new MayPayManaEffect(
                "{1}{U}",
                new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent(),
                "Pay {1}{U} to create a token that's a copy of that artifact?"
        ));
    }
}
