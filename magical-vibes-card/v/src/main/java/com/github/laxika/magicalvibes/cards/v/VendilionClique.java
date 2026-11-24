package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "55")
@CardRegistration(set = "MM2", collectorNumber = "67")
@CardRegistration(set = "MMA", collectorNumber = "70")
@CardRegistration(set = "A25", collectorNumber = "76")
@CardRegistration(set = "SLD", collectorNumber = "118")
@CardRegistration(set = "HA7", collectorNumber = "5")
public class VendilionClique extends Card {

    public VendilionClique() {
        // When Vendilion Clique enters, look at target player's hand. You may choose a nonland card from it. If you
        // do, that player reveals the chosen card, puts it on the bottom of their library, then draws a card.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new ChooseCardsFromTargetHandEffect(new Fixed(1), List.of(CardType.LAND), List.of(),
                        HandChoiceDestination.KEEP_IN_HAND, false, null, 0, true, false, false, false, false),
                new ConditionalEffect(new HappenedThisWay(StepKind.CHOOSE), SequenceEffect.of(
                        RevealCardsFromHandEffect.cards(Subjects.targetPlayerDeclaredElsewhere(),
                                Subjects.cardsThisWay(StepKind.CHOOSE)),
                        PutCardsIntoLibraryEffect.onBottom(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new DrawCardEffect(1, Subjects.targetPlayerDeclaredElsewhere())))));
    }
}
