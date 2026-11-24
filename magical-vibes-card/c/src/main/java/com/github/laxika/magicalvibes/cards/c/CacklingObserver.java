package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealMatchingCardsFromTargetHandAndKeepEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostAmountPredicate;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TrackChosenCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "9")
public class CacklingObserver extends Card {

    public CacklingObserver() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new RevealMatchingCardsFromTargetHandAndKeepEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), false),
                        new ConditionalEffect(HappenedThisWay.exiled(), new TrackChosenCardExiledWithSourceEffect())));

        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new SeekLibraryEffect(1, new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new CardManaValueAtMostAmountPredicate(new Sum(
                                new GreatestManaValueAmongCardsExiledWithSource(), new Fixed(-1))))))
                        .by(Subjects.ownersOf(Subjects.exiledWithSource())));
    }
}
