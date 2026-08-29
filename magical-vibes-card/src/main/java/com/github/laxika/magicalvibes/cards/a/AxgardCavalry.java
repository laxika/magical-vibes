package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "189")
@CardRegistration(set = "KHM", collectorNumber = "121")
public class AxgardCavalry extends Card {

    public AxgardCavalry() {
        // {T}: Target creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{T}: Target creature gains haste until end of turn.",
                TargetFilters.creature()));
    }
}
