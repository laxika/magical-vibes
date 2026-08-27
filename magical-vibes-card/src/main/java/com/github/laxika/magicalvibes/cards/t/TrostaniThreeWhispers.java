package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "238")
@CardRegistration(set = "MKM", collectorNumber = "322")
@CardRegistration(set = "MKM", collectorNumber = "372")
@CardRegistration(set = "MKM", collectorNumber = "388")
public class TrostaniThreeWhispers extends Card {

    public TrostaniThreeWhispers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                "{1}{G}: Target creature gains deathtouch until end of turn.",
                TargetFilters.creature()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G/W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET)),
                "{G/W}: Target creature gains vigilance until end of turn.",
                TargetFilters.creature()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET)),
                "{2}{W}: Target creature gains double strike until end of turn.",
                TargetFilters.creature()));
    }
}
