package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "107")
public class GreenbeltRampager extends Card {

    public GreenbeltRampager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ForcedCostOrElseEffect(
                        new PayEnergyCost(2),
                        List.of(ReturnToHandEffect.self(), new EnergyCountersEffect(1))));
    }
}
