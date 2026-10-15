package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "115")
public class WeatherseedElf extends Card {

    public WeatherseedElf() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.TARGET)),
                "{T}: Target creature gains forestwalk until end of turn.",
                TargetFilters.creature()));
    }
}
