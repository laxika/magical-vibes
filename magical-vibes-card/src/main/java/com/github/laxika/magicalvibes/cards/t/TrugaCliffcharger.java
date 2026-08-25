package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class TrugaCliffcharger extends Card {

    public TrugaCliffcharger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.LAND),
                                        new CardTypePredicate(CardType.BATTLE))),
                                LibrarySearchDestination.HAND),
                        "a card"),
                "Discard a card to search your library for a land or battle card?"));
    }
}
