package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "248")
@CardRegistration(set = "ONS", collectorNumber = "328")
public class WindsweptHeath extends Card {

    public WindsweptHeath() {
        // {T}, Pay 1 life, Sacrifice this land: Search your library for a Forest or Plains card,
        // put it onto the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.FOREST),
                                        new CardSubtypePredicate(CardSubtype.PLAINS))),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{T}, Pay 1 life, Sacrifice Windswept Heath: Search your library for a Forest or Plains card, put it onto the battlefield, then shuffle."
        ));
    }
}
