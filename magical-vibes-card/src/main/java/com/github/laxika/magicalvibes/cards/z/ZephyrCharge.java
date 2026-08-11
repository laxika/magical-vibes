package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "82")
@CardRegistration(set = "M20", collectorNumber = "82")
public class ZephyrCharge extends Card {

    public ZephyrCharge() {
        // {1}{U}: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{1}{U}: Target creature gains flying until end of turn.",
                TargetFilters.creature()));
    }
}
