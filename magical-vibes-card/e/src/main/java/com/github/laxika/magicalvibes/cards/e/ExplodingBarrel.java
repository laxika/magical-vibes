package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "39")
public class ExplodingBarrel extends Card {

    public ExplodingBarrel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardAnyColorManaEffect(),
                        new PutCountersOnSelfEffect(CounterType.PRESSURE)
                ),
                "{T}: Add one mana of any color. Put a pressure counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(
                        new ReduceActivationCostEffect(new CountersOnSource(CounterType.PRESSURE)),
                        new SacrificeSelfCost(),
                        new DealDamageToTargetCreatureEffect(20)
                ),
                "{8}, {T}, Sacrifice this artifact: It deals 20 damage to target creature. "
                        + "This ability costs {1} less to activate for each pressure counter on this artifact. "
                        + "Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
