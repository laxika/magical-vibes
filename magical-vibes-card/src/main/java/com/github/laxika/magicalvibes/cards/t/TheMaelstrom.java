package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "OHOP", collectorNumber = "23")
public class TheMaelstrom extends Card {

    public TheMaelstrom() {
        var revealPermanent = new RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(
                new CardIsPermanentPredicate());
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, revealPermanent);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, revealPermanent);
        addEffect(EffectSlot.CHAOS_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build());
    }
}
