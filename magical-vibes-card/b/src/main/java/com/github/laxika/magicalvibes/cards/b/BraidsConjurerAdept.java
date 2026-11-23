package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "36")
@CardRegistration(set = "2XM", collectorNumber = "43")
@CardRegistration(set = "CMM", collectorNumber = "76")
@CardRegistration(set = "CMM", collectorNumber = "481")
public class BraidsConjurerAdept extends Card {

    public BraidsConjurerAdept() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new MayEffect(
                SequenceEffect.of(
                        ChooseCardEffect.oneOf(
                                        Subjects.cardsInHands(Subjects.activePlayer(), new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.ARTIFACT),
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardTypePredicate(CardType.LAND)))),
                                        "an artifact, creature, or land card from your hand to put onto the battlefield")
                                .by(Subjects.activePlayer()),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                "Put an artifact, creature, or land card from your hand onto the battlefield?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
