package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "PCY", collectorNumber = "30")
public class AvatarOfWill extends Card {

    public AvatarOfWill() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnOpponentHandEmpty(), new ReduceOwnCastCostEffect(new Fixed(6))));
    }
}
