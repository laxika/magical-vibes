package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "121")
public class LathnuHellion extends Card {

    public LathnuHellion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EnergyCountersEffect(2));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ForcedCostOrElseEffect(new PayEnergyCost(2), List.of(new SacrificeSelfEffect()), true));
    }
}
