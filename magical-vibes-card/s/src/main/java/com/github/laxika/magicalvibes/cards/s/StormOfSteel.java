package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "TMC", collectorNumber = "120")
public class StormOfSteel extends Card {

    public StormOfSteel() {
        // Storm of Steel deals 2 damage to each of one or two targets.
        target(1, 2).addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new Fixed(2)));
    }
}
