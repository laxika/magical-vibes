package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "120")
public class BloodlustInciter extends Card {

    public BloodlustInciter() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{T}: Target creature gains haste until end of turn.",
                TargetFilters.creature()));
    }
}
