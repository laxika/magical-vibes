package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "186")
public class ShefetMonitor extends Card {

    public ShefetMonitor() {
        // Cycling {3}{G} ({3}{G}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, you may search your library for a basic land card or a Desert
        // card, put it onto the battlefield, then shuffle. (Do this before you draw.)" The reflexive
        // cycle trigger is folded onto the cycling ability: the may-search resolves first (untapped
        // onto the battlefield), then the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(false, "{3}{G}",
                List.of(
                        new MayEffect(new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardSubtypePredicate(CardSubtype.DESERT))),
                                LibrarySearchDestination.BATTLEFIELD),
                                "Search your library for a basic land or Desert card?"),
                        new DrawCardEffect(1)),
                "Cycling {3}{G} ({3}{G}, Discard this card: Draw a card.)"));
    }
}
