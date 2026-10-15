package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "120")
public class DauthiEmbrace extends Card {

    public DauthiEmbrace() {
        // {B}{B}: Target creature gains shadow until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{B}{B}",
                List.of(new GrantKeywordEffect(Keyword.SHADOW, GrantScope.TARGET)),
                "{B}{B}: Target creature gains shadow until end of turn.",
                TargetFilters.creature()));
    }
}
