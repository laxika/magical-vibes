package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "EOE", collectorNumber = "195")
public class LashwhipPredator extends Card {

    public LashwhipPredator() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                new ReduceOwnCastCostEffect(new Fixed(2))));
    }
}
