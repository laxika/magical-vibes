package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "226")
public class VialOfPoison extends Card {

    public VialOfPoison() {
        // {1}, Sacrifice this artifact: Target creature gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                "{1}, Sacrifice this artifact: Target creature gains deathtouch until end of turn.",
                TargetFilters.creature()));
    }
}
