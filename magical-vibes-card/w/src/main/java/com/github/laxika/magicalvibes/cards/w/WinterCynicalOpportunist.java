package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "7")
public class WinterCynicalOpportunist extends Card {

    public WinterCynicalOpportunist() {
        addTrigger(Triggers.attacks(), new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                new ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect(),
                "Exile any number of cards from your graveyard?"));
    }
}
