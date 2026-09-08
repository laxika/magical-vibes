package com.github.laxika.magicalvibes.cards.i;

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

@CardRegistration(set = "CSP", collectorNumber = "111")
public class IntoTheNorth extends Card {

    public IntoTheNorth() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.SNOW),
                        new CardTypePredicate(CardType.LAND))),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
