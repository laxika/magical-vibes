package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "96")
public class CaptiveFlame extends Card {

    public CaptiveFlame() {
        addActivatedAbility(new ActivatedAbility(
                false, "{R}",
                List.of(new BoostTargetCreatureEffect(1, 0)),
                "{R}: Target creature gets +1/+0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
