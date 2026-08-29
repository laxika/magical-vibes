package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "155")
public class FlowstoneChanneler extends Card {

    public FlowstoneChanneler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostTargetCreatureEffect(1, -1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                ),
                "{1}{R}, {T}, Discard a card: Target creature gets +1/-1 and gains haste until end of turn.",
                TargetFilters.creature()
        ));
    }
}
