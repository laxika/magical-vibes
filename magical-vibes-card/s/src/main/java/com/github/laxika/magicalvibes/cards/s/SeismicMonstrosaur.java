package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "166")
public class SeismicMonstrosaur extends Card {

    public SeismicMonstrosaur() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new DrawCardEffect(1)
                ),
                "{2}{R}, Sacrifice a land: Draw a card."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.MOUNTAIN))),
                "Mountaincycling {2} ({2}, Discard this card: Search your library for a Mountain card, "
                        + "reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
