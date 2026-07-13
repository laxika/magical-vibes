package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;

@CardRegistration(set = "7ED", collectorNumber = "243")
@CardRegistration(set = "6ED", collectorNumber = "228")
public class FamiliarGround extends Card {

    public FamiliarGround() {
        // "Each creature you control can't be blocked by more than one creature."
        addEffect(EffectSlot.STATIC, new EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
