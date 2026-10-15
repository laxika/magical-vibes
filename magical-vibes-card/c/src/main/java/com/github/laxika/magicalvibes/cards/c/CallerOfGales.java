package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "43")
public class CallerOfGales extends Card {

    public CallerOfGales() {
        // {1}{U}, {T}: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{1}{U}, {T}: Target creature gains flying until end of turn.",
                TargetFilters.creature()));
    }
}
