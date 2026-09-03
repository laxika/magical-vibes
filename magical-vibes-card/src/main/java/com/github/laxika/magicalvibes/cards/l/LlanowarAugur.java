package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "129")
public class LlanowarAugur extends Card {

    public LlanowarAugur() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new BoostTargetCreatureEffect(3, 3),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "{G}, Sacrifice this creature: Target creature gets +3/+3 and gains trample until end of turn. "
                        + "Activate only during your upkeep.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
