package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "248")
public class GamePreserve extends Card {

    public GamePreserve() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.eachPlayer()),
                new ConditionalEffect(
                        new NotCondition(HappenedThisWay.toACard(StepKind.REVEAL,
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.REVEAL)))));
    }
}
