package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "228")
@CardRegistration(set = "MOM", collectorNumber = "309")
public class HidetsuguAndKairi extends Card {

    public HidetsuguAndKairi() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(3),
                        TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.yours(2, HandToLibraryPlacement.TOP));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.dies(ObjectRelation.THIS),
                SequenceEffect.of(
                        new ExileTopCardOfOwnLibraryEffect(false),
                        new LoseLifeEffect(AmountThisWay.exiledManaValue(), Subjects.targetPlayer()),
                        new MayCastDiscardedCardWithoutPayingManaCostEffect(Subjects.cardsThisWay(StepKind.EXILE),
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))))));
    }
}
