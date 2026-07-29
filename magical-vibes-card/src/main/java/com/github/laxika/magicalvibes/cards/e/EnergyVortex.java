package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DamageTargetPlayerUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "64")
public class EnergyVortex extends Card {

    public EnergyVortex() {
        // "As this enchantment enters, choose an opponent" — in a two-player game the chosen player
        // is always the single opponent, so it is modelled implicitly (as Cursed Rack does).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RemoveAllCountersFromSelfEffect(CounterType.VORTEX));

        // At the beginning of the chosen player's upkeep, deal 3 damage to them unless they pay {1}
        // for each vortex counter.
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new DamageTargetPlayerUnlessPaysPerCounterEffect(3, CounterType.VORTEX, "{1}"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new PutCountersOnSelfEffect(CounterType.VORTEX, new XValue())),
                "{X}: Put X vortex counters on Energy Vortex. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
