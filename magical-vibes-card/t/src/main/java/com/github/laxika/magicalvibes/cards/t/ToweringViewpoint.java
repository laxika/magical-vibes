package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "77")
public class ToweringViewpoint extends Card {

    public ToweringViewpoint() {
        // {3}: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{3}: Target creature gains flying until end of turn.",
                TargetFilters.creature()
        ));
    }
}
