package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "AER", collectorNumber = "116")
public class MonstrousOnslaught extends Card {

    public MonstrousOnslaught() {
        // Monstrous Onslaught deals X damage divided as you choose among any number of target
        // creatures, where X is the greatest power among creatures you control as you cast this spell.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(
                new GreatestPowerAmongControlled()));
    }
}
