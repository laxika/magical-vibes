package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "MBS", collectorNumber = "114")
public class Mirrorworks extends Card {

    public Mirrorworks() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{2}",
                new CreateTokenCopyOfTargetPermanentEffect(),
                "Pay {2} to create a token that's a copy of that artifact?"
        ));
    }
}
