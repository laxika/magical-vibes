package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "172")
public class FaunaShaman extends Card {

    public FaunaShaman() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new DiscardCardTypeCost(CardType.CREATURE),
                        new SearchLibraryForCardTypesToHandEffect(Set.of(CardType.CREATURE))
                ),
                "{G}, {T}, Discard a creature card: Search your library for a creature card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
