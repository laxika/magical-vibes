package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "23")
@CardRegistration(set = "OPCA", collectorNumber = "49")
public class TheMaelstrom extends Card {

    public TheMaelstrom() {
        var revealPermanent = new RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(
                new CardIsPermanentPredicate());
        addTrigger(Triggers.planeswalkToThis(), revealPermanent);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), revealPermanent);
        addTrigger(Triggers.chaosEnsues(), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .chooseAuraAttachment(true)
                .build());
    }
}
