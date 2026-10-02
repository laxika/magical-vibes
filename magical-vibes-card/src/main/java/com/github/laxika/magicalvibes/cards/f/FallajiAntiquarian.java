package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffect;

@CardRegistration(set = "YBRO", collectorNumber = "8")
public class FallajiAntiquarian extends Card {

    public FallajiAntiquarian() {
        target(1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffect());
    }
}
