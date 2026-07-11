package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "265")
public class WanderersTwig extends Card {

    public WanderersTwig() {
        // {1}, Sacrifice Wanderer's Twig: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "{1}, Sacrifice Wanderer's Twig: Search your library for a basic land card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
