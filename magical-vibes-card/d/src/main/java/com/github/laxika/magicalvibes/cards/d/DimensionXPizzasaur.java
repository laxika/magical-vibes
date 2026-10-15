package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetPermanentThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControlledCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "16")
public class DimensionXPizzasaur extends Card {

    public DimensionXPizzasaur() {
        PermanentAllOfPredicate eligibleDestructionTarget = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentManaValueAtMostControlledCountersPredicate()));

        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnTargetPermanentThenReflexiveEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        2,
                        null,
                        new DestroyTargetPermanentEffect(eligibleDestructionTarget),
                        true));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new GainLifeEffect(3),
                        new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT)
                ),
                "{2}, {T}, Sacrifice Dimension X Pizzasaur: You gain 3 life and each opponent loses 3 life."
        ));
    }
}
