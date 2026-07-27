package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;


@CardRegistration(set = "ALA", collectorNumber = "68")
public class CorpseConnoisseur extends Card {

    public CorpseConnoisseur() {
        // When this creature enters, you may search your library for a creature card,
        // put that card into your graveyard, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE),
                        LibrarySearchDestination.GRAVEYARD),
                "Search your library for a creature card and put it into your graveyard?"
        ));

        // Unearth {3}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{3}{B}");
    }
}
