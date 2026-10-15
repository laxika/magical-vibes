package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachSubtypeToHandEffect;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "205")
public class GemOfBecoming extends Card {

    public GemOfBecoming() {
        // {3}, {T}, Sacrifice this artifact: Search your library for an Island card, a Swamp card,
        // and a Mountain card. Reveal those cards, put them into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SacrificeSelfCost(),
                        new SearchLibraryForOneCardOfEachSubtypeToHandEffect(
                                List.of(CardSubtype.ISLAND, CardSubtype.SWAMP, CardSubtype.MOUNTAIN))),
                "{3}, {T}, Sacrifice Gem of Becoming: Search your library for an Island card, a Swamp "
                        + "card, and a Mountain card. Reveal those cards, put them into your hand, then shuffle."));
    }
}
