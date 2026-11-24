package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "215")
public class OildeepGearhulk extends Card {

    public OildeepGearhulk() {
        // When this creature enters, look at target player's hand. You may choose a card from it. If you do, that
        // player discards that card, then draws a card.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new ChooseCardsFromTargetHandEffect(new Fixed(1), List.of(), List.of(),
                        HandChoiceDestination.KEEP_IN_HAND, false, null, 0, true, false, false, false, false),
                new ConditionalEffect(new HappenedThisWay(StepKind.CHOOSE), SequenceEffect.of(
                        new DiscardCardsEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new DrawCardEffect(1, Subjects.targetPlayerDeclaredElsewhere())))));
    }
}
