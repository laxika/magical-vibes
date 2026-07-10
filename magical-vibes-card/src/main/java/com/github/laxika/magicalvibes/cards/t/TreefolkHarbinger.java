package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "239")
public class TreefolkHarbinger extends Card {

    public TreefolkHarbinger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.TREEFOLK),
                        new CardSubtypePredicate(CardSubtype.FOREST))),
                        LibrarySearchDestination.TOP_OF_LIBRARY),
                "Search your library for a Treefolk or Forest card, reveal it, then shuffle and put that card on top?"
        ));
    }
}
