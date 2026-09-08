package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "66")
public class RakaDisciple extends Card {

    public RakaDisciple() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(PreventDamageEffect.nextToTarget(1)),
                "{W}, {T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{U}, {T}: Target creature gains flying until end of turn.",
                TargetFilters.creature()
        ));
    }
}
