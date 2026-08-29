package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "26")
public class AdarkarWindform extends Card {

    public AdarkarWindform() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{S}",
                List.of(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{1}{S}: Target creature loses flying until end of turn.",
                TargetFilters.creature()
        ));
    }
}
