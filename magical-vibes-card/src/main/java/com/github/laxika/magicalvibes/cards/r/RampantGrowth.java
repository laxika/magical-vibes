package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "262")
@CardRegistration(set = "8ED", collectorNumber = "274")
@CardRegistration(set = "10E", collectorNumber = "288")
@CardRegistration(set = "9ED", collectorNumber = "263")
@CardRegistration(set = "M10", collectorNumber = "201")
@CardRegistration(set = "6ED", collectorNumber = "246")
public class RampantGrowth extends Card {

    public RampantGrowth() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(new CardSupertypePredicate(CardSupertype.BASIC), new CardTypePredicate(CardType.LAND))),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
