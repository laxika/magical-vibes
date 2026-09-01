package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "SNC", collectorNumber = "255")
public class RiveteersOverlook extends Card {

    public RiveteersOverlook() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SacrificeSelfThenEffect.reflexive(SequenceEffect.of(
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardAnyOfPredicate(List.of(
                                                new CardSubtypePredicate(CardSubtype.SWAMP),
                                                new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                                                new CardSubtypePredicate(CardSubtype.FOREST))))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        new GainLifeEffect(1))));
    }
}
