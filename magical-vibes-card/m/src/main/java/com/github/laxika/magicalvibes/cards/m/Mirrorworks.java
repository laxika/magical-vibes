package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "114")
@CardRegistration(set = "BRC", collectorNumber = "149")
@CardRegistration(set = "C18", collectorNumber = "211")
public class Mirrorworks extends Card {

    public Mirrorworks() {
        addTrigger(Triggers.enters(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayPayManaEffect(
                "{2}",
                new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent(),
                "Pay {2} to create a token that's a copy of that artifact?"
        ));
    }
}
