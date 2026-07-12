package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "8ED", collectorNumber = "4")
public class AvatarOfHope extends Card {

    public AvatarOfHope() {
        // If you have 3 or less life, this spell costs {6} less to cast.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerLifeAtMost(3), new ReduceOwnCastCostEffect(new Fixed(6))));

        // This creature can block any number of creatures.
        addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
    }
}
