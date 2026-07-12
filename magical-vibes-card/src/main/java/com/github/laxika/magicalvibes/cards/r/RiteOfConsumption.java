package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "SHM", collectorNumber = "76")
public class RiteOfConsumption extends Card {

    public RiteOfConsumption() {
        // As an additional cost to cast this spell, sacrifice a creature; snapshots its power into xValue.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));

        // Deals damage equal to the sacrificed creature's power to target player or planeswalker...
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(new XValue()));

        // ...and you gain life equal to the damage dealt this way.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
