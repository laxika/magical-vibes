package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "M13", collectorNumber = "132")
public class FlamesOfTheFirebrand extends Card {

    public FlamesOfTheFirebrand() {
        // Flames of the Firebrand deals 3 damage divided as you choose among one, two, or three targets.
        // Each target must be assigned at least 1 damage, so a total of 3 caps the target count at three.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(3));
    }
}
