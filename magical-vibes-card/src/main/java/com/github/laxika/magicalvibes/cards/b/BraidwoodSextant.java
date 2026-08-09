package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "127")
public class BraidwoodSextant extends Card {

    public BraidwoodSextant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "{2}, {T}, Sacrifice Braidwood Sextant: Search your library for a basic land card, reveal that card, put it into your hand, then shuffle."
        ));
    }
}
