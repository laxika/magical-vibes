package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "26")
public class SvyelunitePriest extends Card {

    public SvyelunitePriest() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.TARGET)),
                "{U}{U}, {T}: Target creature gains shroud until end of turn. Activate only during your upkeep.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
