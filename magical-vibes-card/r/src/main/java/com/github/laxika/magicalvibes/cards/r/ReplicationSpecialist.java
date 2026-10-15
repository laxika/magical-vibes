package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "NEO", collectorNumber = "76")
public class ReplicationSpecialist extends Card {

    public ReplicationSpecialist() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{1}{U}",
                new CreateTokenCopyOfTargetPermanentEffect(),
                "Pay {1}{U} to create a token that's a copy of that artifact?"
        ));
    }
}
