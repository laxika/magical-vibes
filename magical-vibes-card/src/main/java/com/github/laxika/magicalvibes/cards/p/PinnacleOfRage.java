package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "BNG", collectorNumber = "105")
public class PinnacleOfRage extends Card {

    public PinnacleOfRage() {
        target(2, 2)
                .addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new Fixed(3)));
    }
}
