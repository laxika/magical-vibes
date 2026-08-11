package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "215")
public class ThornscapeApprentice extends Card {

    public ThornscapeApprentice() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "{R}, {T}: Target creature gains first strike until end of turn.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{W}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
