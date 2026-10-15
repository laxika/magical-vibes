package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "71")
@CardRegistration(set = "TPR", collectorNumber = "56")
public class LegacysAllure extends Card {

    public LegacysAllure() {
        // At the beginning of your upkeep, you may put a treasure counter on this enchantment.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.TREASURE),
                "Put a treasure counter on Legacy's Allure?"
        ));

        // Sacrifice this enchantment: Gain control of target creature with power less than or equal
        // to the number of treasure counters on this enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                "Sacrifice Legacy's Allure: Gain control of target creature with power less than or "
                        + "equal to the number of treasure counters on Legacy's Allure.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostSourceCountersPredicate(CounterType.TREASURE)
                        )),
                        "Target must be a creature with power less than or equal to the number of "
                                + "treasure counters on Legacy's Allure"
                )
        ));
    }
}
