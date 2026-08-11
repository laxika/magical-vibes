package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "ECL", collectorNumber = "196")
public class SurlyFarrier extends Card {

    public SurlyFarrier() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(
                        new BoostTargetCreatureEffect(1, 1),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET)
                ),
                "{T}: Target creature you control gets +1/+1 and gains vigilance until end of turn. Activate only as a sorcery.",
                TargetFilters.creatureYouControl(), null, null, ActivationTimingRestriction.SORCERY_SPEED));
    }
}
