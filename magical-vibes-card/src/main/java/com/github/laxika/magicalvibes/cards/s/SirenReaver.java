package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "RIX", collectorNumber = "54")
public class SirenReaver extends Card {

    public SirenReaver() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Raid(), new ReduceOwnCastCostEffect(new Fixed(1))));
    }
}
