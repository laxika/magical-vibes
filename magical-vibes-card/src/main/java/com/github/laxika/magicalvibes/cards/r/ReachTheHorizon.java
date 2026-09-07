package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "195")
public class ReachTheHorizon extends Card {

    public ReachTheHorizon() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(2),
                new CardAnyOfPredicate(List.of(
                        CardPredicateUtils.basicLand(),
                        new CardSubtypePredicate(CardSubtype.TOWN))),
                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                null,
                true));
    }
}
