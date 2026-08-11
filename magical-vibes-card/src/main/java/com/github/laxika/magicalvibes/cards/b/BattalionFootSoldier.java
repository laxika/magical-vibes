package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "M20", collectorNumber = "7")
public class BattalionFootSoldier extends Card {

    public BattalionFootSoldier() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new CardsInLibrary(CountScope.CONTROLLER),
                        new CardNamedPredicate("Battalion Foot Soldier"),
                        LibrarySearchDestination.HAND),
                "Search your library for any number of cards named Battalion Foot Soldier?"
        ));
    }
}
