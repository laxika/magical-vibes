package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "73")
public class RazakethTheFoulblooded extends Card {

    public RazakethTheFoulblooded() {
        // Pay 2 life, Sacrifice another creature: Search your library for a card,
        // put that card into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new PayLifeCost(2),
                        new SacrificeCreatureCost(false, false, false, true),
                        new SearchLibraryEffect()
                ),
                "Pay 2 life, Sacrifice another creature: Search your library for a card, put that card into your hand, then shuffle."
        ));
    }
}
