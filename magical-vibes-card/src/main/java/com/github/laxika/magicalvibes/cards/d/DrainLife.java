package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "5ED", collectorNumber = "156")
@CardRegistration(set = "4ED", collectorNumber = "132")
public class DrainLife extends Card {

    public DrainLife() {
        // Deals X damage to any target... (the "spend only black mana on X" payment
        // restriction is a flavor nuance the mana engine does not model, per Crypt Rats).
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));

        // ...and you gain life equal to the damage dealt (as with Rite of Consumption).
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
