package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "263")
public class EnergyBolt extends Card {

    public EnergyBolt() {
        // Modal spell with an {X} cost: the cast xValue slot carries the mode index, so the paid X
        // is threaded separately (playModalXCard) and read at resolution via XValue.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Energy Bolt deals X damage to target player or planeswalker",
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(new XValue())),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains X life",
                        new TargetPlayerGainsLifeEffect(new XValue()))
        )));
    }
}
