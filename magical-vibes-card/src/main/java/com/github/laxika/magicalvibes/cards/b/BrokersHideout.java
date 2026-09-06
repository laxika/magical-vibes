package com.github.laxika.magicalvibes.cards.b;

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

@CardRegistration(set = "SNC", collectorNumber = "248")
public class BrokersHideout extends Card {

    public BrokersHideout() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SacrificeSelfThenEffect.reflexive(SequenceEffect.of(
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardAnyOfPredicate(List.of(
                                                new CardSubtypePredicate(CardSubtype.FOREST),
                                                new CardSubtypePredicate(CardSubtype.PLAINS),
                                                new CardSubtypePredicate(CardSubtype.ISLAND))))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        new GainLifeEffect(1))));
    }
}
