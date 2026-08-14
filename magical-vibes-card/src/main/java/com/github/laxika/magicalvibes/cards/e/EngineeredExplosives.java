package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "118")
public class EngineeredExplosives extends Card {

    public EngineeredExplosives() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.CHARGE, new XValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect()
                ),
                "{2}, Sacrifice Engineered Explosives: Destroy each nonland permanent with mana value equal to the number of charge counters on Engineered Explosives."
        ));
    }
}
