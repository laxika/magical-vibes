package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "113")
@CardRegistration(set = "TPR", collectorNumber = "179")
@CardRegistration(set = "MB2", collectorNumber = "208")
public class Manabond extends Card {

    public Manabond() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        RevealCardsFromHandEffect.hand(Subjects.you()),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsInYourHand(new CardTypePredicate(CardType.LAND))),
                        new DiscardHandEffect()),
                "Reveal your hand and put all land cards from it onto the battlefield?"));
    }
}
