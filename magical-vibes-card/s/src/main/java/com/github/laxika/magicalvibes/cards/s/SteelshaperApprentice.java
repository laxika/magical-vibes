package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "15")
public class SteelshaperApprentice extends Card {

    public SteelshaperApprentice() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new ReturnSelfToHandCost(),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT))
                ),
                "{W}, {T}, Return this creature to its owner's hand: Search your library for an Equipment card, reveal that card, put it into your hand, then shuffle."
        ));
    }
}
