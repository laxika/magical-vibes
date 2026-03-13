package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageToAnyTargetsEffect;

@CardRegistration(set = "M10", collectorNumber = "127")
public class BogardanHellkite extends Card {

    public BogardanHellkite() {
        // When Bogardan Hellkite enters the battlefield, it deals 5 damage
        // divided as you choose among any number of targets.
        // (Each target must receive at least 1, so max 5 targets.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDividedDamageToAnyTargetsEffect(5, 5));
    }
}
