package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "147")
public class SpoilsOfVictory extends Card {

    public SpoilsOfVictory() {
        // Any card with a basic land type (basics or dual lands), onto the battlefield untapped.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        new CardSubtypePredicate(CardSubtype.ISLAND),
                        new CardSubtypePredicate(CardSubtype.SWAMP),
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        new CardSubtypePredicate(CardSubtype.FOREST))),
                LibrarySearchDestination.BATTLEFIELD));
    }
}
