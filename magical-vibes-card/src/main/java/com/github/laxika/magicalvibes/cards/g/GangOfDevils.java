package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "AVR", collectorNumber = "136")
public class GangOfDevils extends Card {

    public GangOfDevils() {
        // When this creature dies, it deals 3 damage divided as you choose among one, two, or three targets.
        addEffect(EffectSlot.ON_DEATH, DealDividedDamageEffect.chosenAmongAnyTargetsEtb(3, 3));
    }
}
