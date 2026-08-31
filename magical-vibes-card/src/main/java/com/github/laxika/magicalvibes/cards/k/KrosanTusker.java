package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "272")
public class KrosanTusker extends Card {

    public KrosanTusker() {
        // Cycling {2}{G} ({2}{G}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // When you cycle this card, you may search your library for a basic land card, reveal that
        // card, put it into your hand, then shuffle. The search resolves before the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(
                        new MayEffect(new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND),
                                "Search your library for a basic land card?"),
                        new DrawCardEffect(1)),
                "Cycling {2}{G} ({2}{G}, Discard this card: Draw a card.)"));
    }
}
