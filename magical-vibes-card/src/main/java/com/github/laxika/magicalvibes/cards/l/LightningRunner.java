package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "AER", collectorNumber = "90")
public class LightningRunner extends Card {

    public LightningRunner() {
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new EnergyCountersEffect(2),
                new MayEffect(
                        ConditionalEffect.unless(new ControllerEnergyAtLeast(8),
                                SequenceEffect.of(
                                        new EnergyCountersEffect(-8),
                                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                                                new PermanentIsCreaturePredicate()),
                                        new AdditionalCombatPhaseEffect(1))),
                        "Pay {E}{E}{E}{E}{E}{E}{E}{E} to untap all creatures you control and get an additional combat phase?"
                )
        ));
    }
}
