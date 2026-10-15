package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerSpeed;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureOrVehicleOrDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "97")
public class MomentumBreaker extends Card {

    public MomentumBreaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentSacrificesCreatureOrVehicleOrDiscardsEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(new ControllerSpeed())),
                "{2}, Sacrifice this enchantment: You gain life equal to your speed."
        ));
    }
}
