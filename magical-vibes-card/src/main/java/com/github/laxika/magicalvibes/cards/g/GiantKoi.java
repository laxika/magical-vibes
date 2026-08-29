package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "53")
public class GiantKoi extends Card {

    public GiantKoi() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new WaterbendCost(3), new MakeCreatureUnblockableEffect(true)),
                "Waterbend {3}: This creature can't be blocked this turn."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ISLAND))),
                "Islandcycling {2} ({2}, Discard this card: Search your library for an Island card, "
                        + "reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
