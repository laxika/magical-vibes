package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "135")
@CardRegistration(set = "MMQ", collectorNumber = "173")
public class BattleRampart extends Card {

    public BattleRampart() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{T}: Target creature gains haste until end of turn.",
                TargetFilters.creature()));
    }
}
