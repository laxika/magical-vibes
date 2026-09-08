package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "213")
public class WildheartInvoker extends Card {

    public WildheartInvoker() {
        // {8}: Target creature gets +5/+5 and gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(
                        new BoostTargetCreatureEffect(5, 5),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "{8}: Target creature gets +5/+5 and gains trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
