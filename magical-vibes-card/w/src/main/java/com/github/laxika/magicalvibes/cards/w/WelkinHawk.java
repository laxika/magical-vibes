package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "25")
public class WelkinHawk extends Card {

    public WelkinHawk() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("Welkin Hawk"), LibrarySearchDestination.HAND),
                "Search your library for a card named Welkin Hawk?"
        ));
    }
}
