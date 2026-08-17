package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "265")
public class RushwoodHerbalist extends Card {

    public RushwoodHerbalist() {
        // {G}, {T}, Discard a card: Regenerate target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new DiscardCardTypeCost(null, null), new RegenerateEffect(true)),
                "{G}, {T}, Discard a card: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}
