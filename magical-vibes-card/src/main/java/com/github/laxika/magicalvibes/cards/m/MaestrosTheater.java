package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "251")
public class MaestrosTheater extends Card {

    public MaestrosTheater() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SacrificeSelfThenEffect.reflexive(SequenceEffect.of(
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardAnyOfPredicate(List.of(
                                                new CardSubtypePredicate(CardSubtype.ISLAND),
                                                new CardSubtypePredicate(CardSubtype.SWAMP),
                                                new CardSubtypePredicate(CardSubtype.MOUNTAIN))))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        new GainLifeEffect(1))));
    }
}
