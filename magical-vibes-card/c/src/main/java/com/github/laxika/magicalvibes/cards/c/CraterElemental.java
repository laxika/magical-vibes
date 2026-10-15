package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "132")
public class CraterElemental extends Card {

    public CraterElemental() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(4)),
                "{R}, {T}, Sacrifice this creature: It deals 4 damage to target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SetBasePowerToughnessEffect(8, null, GrantScope.SELF)),
                "{2}{R}: This creature has base power 8 until end of turn."
        ).withActivationCondition(
                new ControlledCreaturesTotalPowerAtLeast(8),
                "Activate only if creatures you control have total power 8 or greater."
        ));
    }
}
