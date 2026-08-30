package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "3")
public class BraveKinDuo extends Card {

    public BraveKinDuo() {
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{1}, {T}: Target creature gets +1/+1 until end of turn.",
                TargetFilters.creature(), null, null, ActivationTimingRestriction.SORCERY_SPEED));
    }
}
