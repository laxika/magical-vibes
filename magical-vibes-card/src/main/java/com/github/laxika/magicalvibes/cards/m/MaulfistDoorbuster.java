package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "123")
public class MaulfistDoorbuster extends Card {

    public MaulfistDoorbuster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));

        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(1),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-1),
                                new CantBlockThisTurnEffect(TapUntapScope.TARGET))),
                "Pay {E} so target creature can't block this turn?"));
    }
}
