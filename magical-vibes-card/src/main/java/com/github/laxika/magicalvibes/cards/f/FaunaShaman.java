package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "172")
public class FaunaShaman extends Card {

    public FaunaShaman() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.CREATURE), "creature"),
                        new SearchLibraryForCardTypesToHandEffect(
                                new CardTypePredicate(CardType.CREATURE))
                ),
                "{G}, {T}, Discard a creature card: Search your library for a creature card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
