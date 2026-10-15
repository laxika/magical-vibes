package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "56")
public class DauthiTrapper extends Card {

    public DauthiTrapper() {
        // {T}: Target creature gains shadow until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GrantKeywordEffect(Keyword.SHADOW, GrantScope.TARGET)),
                "{T}: Target creature gains shadow until end of turn.",
                TargetFilters.creature()));
    }
}
