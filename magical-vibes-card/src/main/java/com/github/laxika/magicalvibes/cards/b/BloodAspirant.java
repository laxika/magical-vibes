package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "128")
public class BloodAspirant extends Card {

    public BloodAspirant() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new PutCountersOnSourceEffect(1, 1, 1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsEnchantmentPredicate()
                                )),
                                "Sacrifice a creature or enchantment",
                                false
                        ),
                        new DealDamageToTargetCreatureEffect(1),
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET)
                ),
                "{1}{R}, {T}, Sacrifice a creature or enchantment: This creature deals 1 damage to target creature. That creature can't block this turn.",
                TargetFilters.creature()
        ));
    }
}
