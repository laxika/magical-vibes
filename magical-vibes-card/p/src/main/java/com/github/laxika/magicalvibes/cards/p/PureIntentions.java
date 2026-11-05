package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "25")
public class PureIntentions extends Card {

    public PureIntentions() {
        addSpellEffect(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.opponentDiscardsCard(),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .returnAll(true)
                        .discardedByOpponentThisTurn(true)
                        .build())));
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect());
    }
}
