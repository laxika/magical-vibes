package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "85")
@CardRegistration(set = "FUT", collectorNumber = "110")
public class BloodshotTrainee extends Card {

    public BloodshotTrainee() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(4)),
                "{T}: Bloodshot Trainee deals 4 damage to target creature. Activate only if this creature's power is 4 or greater.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.POWER_4_OR_GREATER
        ));
    }
}
