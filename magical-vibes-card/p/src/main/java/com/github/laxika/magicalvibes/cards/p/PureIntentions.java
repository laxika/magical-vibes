package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.TriggeredAbility;

@CardRegistration(set = "SOK", collectorNumber = "25")
public class PureIntentions extends Card {

    public PureIntentions() {
        addSpellEffect(new RegisterGlobalTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.opponentCausesYouToDiscardCard(),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .returnAll(true)
                        .discardedByOpponentThisTurn(true)
                        .build())));
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                RegisterDelayedStepTriggerEffect.aboutCardAtNext(
                        Triggers.endStep(ActivePlayer.ANY), Subjects.sourceCard(),
                        new PutCardsIntoHandEffect(Subjects.sourceCard())));
    }
}
