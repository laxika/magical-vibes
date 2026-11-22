package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "78")
public class ElkinLair extends Card {

    public ElkinLair() {
        // World rule is handled by SBA.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), SequenceEffect.of(
                ExileCardAtRandomEffect.fromHand(Subjects.triggeringPlayer()),
                AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE)).playableByOwner(),
                RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.EXILE)))
                        .onlyIfAnyStillExiled()));
    }
}
