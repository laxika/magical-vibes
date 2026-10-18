package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "186")
@CardRegistration(set = "AKR", collectorNumber = "217")
public class ShefetMonitor extends Card {

    public ShefetMonitor() {
        addCycling("{3}{G}");
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        CardPredicateUtils.basicLand(),
                        new CardSubtypePredicate(CardSubtype.DESERT))),
                LibrarySearchDestination.BATTLEFIELD),
                "Search your library for a basic land or Desert card?"));
    }
}
