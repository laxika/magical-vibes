package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "86")
public class ScarwoodHag extends Card {

    public ScarwoodHag() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{G}{G}{G}",
                List.of(new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.TARGET)),
                "{G}{G}{G}{G}, {T}: Target creature gains forestwalk until end of turn.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RemoveKeywordEffect(Keyword.FORESTWALK, GrantScope.TARGET)),
                "{T}: Target creature loses forestwalk until end of turn.",
                TargetFilters.creature()
        ));
    }
}
