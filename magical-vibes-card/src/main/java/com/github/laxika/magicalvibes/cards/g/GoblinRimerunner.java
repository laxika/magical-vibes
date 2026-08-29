package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "83")
public class GoblinRimerunner extends Card {

    public GoblinRimerunner() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{T}: Target creature can't block this turn.",
                TargetFilters.creature()));
        addActivatedAbility(new ActivatedAbility(false, "{S}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)),
                "{S}: This creature gains haste until end of turn."));
    }
}
