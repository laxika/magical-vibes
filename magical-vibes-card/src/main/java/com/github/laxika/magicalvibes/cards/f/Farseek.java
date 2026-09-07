package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "170")
@CardRegistration(set = "RAV", collectorNumber = "163")
public class Farseek extends Card {

    public Farseek() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        new CardSubtypePredicate(CardSubtype.ISLAND),
                        new CardSubtypePredicate(CardSubtype.SWAMP),
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN))),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
