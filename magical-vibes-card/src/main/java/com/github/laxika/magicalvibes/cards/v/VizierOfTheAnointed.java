package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasEmbalmOrEternalizePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "55")
public class VizierOfTheAnointed extends Card {

    public VizierOfTheAnointed() {
        // When this creature enters, you may search your library for a creature card with eternalize
        // or embalm, put that card into your graveyard, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(1),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardHasEmbalmOrEternalizePredicate())),
                        LibrarySearchDestination.GRAVEYARD),
                "Search your library for a creature card with eternalize or embalm?"));

        // Whenever you activate an eternalize or embalm ability, draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM, new DrawCardEffect(1));
    }
}
