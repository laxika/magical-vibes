package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "284")
public class LifeMatrix extends Card {

    public LifeMatrix() {
        ActivatedAbility regenerationAbility = new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.MATRIX),
                        new RegenerateEffect()
                ),
                "Remove a matrix counter from this creature: Regenerate this creature."
        );

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.MATRIX, 1),
                        new GrantActivatedAbilityEffect(
                                regenerationAbility,
                                GrantScope.TARGET,
                                null,
                                EffectDuration.PERMANENT
                        )
                ),
                "{4}, {T}: Put a matrix counter on target creature and that creature gains \"Remove a matrix counter from this creature: Regenerate this creature.\" Activate only during your upkeep.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
