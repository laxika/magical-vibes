package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsToHandThenDealNonlandManaValueDamageToAnyTargetEffect;

@CardRegistration(set = "DSC", collectorNumber = "353")
public class APremonitionOfYourDemise extends Card {

    public APremonitionOfYourDemise() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsToHandThenDealNonlandManaValueDamageToAnyTargetEffect());
    }
}
