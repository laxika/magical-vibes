package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "SNC", collectorNumber = "13")
public class GatheringThrong extends Card {

    public GatheringThrong() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new CardsInLibrary(CountScope.CONTROLLER),
                        new CardNamedPredicate("Gathering Throng"),
                        LibrarySearchDestination.HAND),
                "Search your library for any number of cards named Gathering Throng?"
        ));
    }
}
