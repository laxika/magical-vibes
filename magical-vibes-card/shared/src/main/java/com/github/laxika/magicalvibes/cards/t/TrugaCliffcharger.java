package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class TrugaCliffcharger extends Card {

    public TrugaCliffcharger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                new SearchLibraryEffect(
                                        new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.LAND),
                                                new CardTypePredicate(CardType.BATTLE))),
                                        LibrarySearchDestination.HAND))),
                "Discard a card to search your library for a land or battle card?"));
    }
}
