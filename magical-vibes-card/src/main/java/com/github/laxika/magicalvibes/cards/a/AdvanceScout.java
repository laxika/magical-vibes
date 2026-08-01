package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "1")
public class AdvanceScout extends Card {

    public AdvanceScout() {
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "{W}: Target creature gains first strike until end of turn.",
                TargetFilters.creature()));
    }
}