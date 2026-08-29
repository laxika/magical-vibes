package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "309")
public class PowerMatrix extends Card {

    public PowerMatrix() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new BoostTargetCreatureEffect(1, 1),
                        new GrantKeywordEffect(
                                Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE),
                                GrantScope.TARGET)
                ),
                "{T}: Target creature gets +1/+1 and gains flying, first strike, and trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
