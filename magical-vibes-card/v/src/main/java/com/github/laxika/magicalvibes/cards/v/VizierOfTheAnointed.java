package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasEmbalmOrEternalizePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "55")
@CardRegistration(set = "AKR", collectorNumber = "88")
public class VizierOfTheAnointed extends Card {

    public VizierOfTheAnointed() {
        // When this creature enters, you may search your library for a creature card with eternalize
        // or embalm, put that card into your graveyard, then shuffle.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(1),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardHasEmbalmOrEternalizePredicate())),
                        LibrarySearchDestination.GRAVEYARD),
                "Search your library for a creature card with eternalize or embalm?"));

        // Whenever you activate an eternalize or embalm ability, draw a card.
        addTrigger(Triggers.youActivateEternalizeOrEmbalm(), new DrawCardEffect(1));
    }
}
