package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "141")
public class KavaronTurbodrone extends Card {

    public KavaronTurbodrone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new BoostTargetCreatureEffect(1, 1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                ),
                "{T}: Target creature you control gets +1/+1 and gains haste until end of turn. Activate only as a sorcery.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
