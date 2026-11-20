package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "107")
public class MoonstoneHarshMistress extends Card {

    public MoonstoneHarshMistress() {
        // Whenever you discard a card, you may exile that card from your graveyard. If you do, until the end of
        // your next turn, you may play that card.
        addTrigger(Triggers.youDiscardCard(), new MayEffect(
                SequenceEffect.of(
                        new ExileTriggeringCardFromGraveyardEffect(),
                        AllowPlayCardsFromExileEffect.untilEndOfYourNextTurn(Subjects.cardsThisWay(StepKind.EXILE))),
                "Exile that card from your graveyard?"));
    }
}
