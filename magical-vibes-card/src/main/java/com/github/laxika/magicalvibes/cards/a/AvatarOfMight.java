package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "10E", collectorNumber = "251")
public class AvatarOfMight extends Card {

    public AvatarOfMight() {
        // Avatar of Might costs {6} less to cast if an opponent controls at least four more
        // creatures than you.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsMoreCreatures(4), new ReduceOwnCastCostEffect(new Fixed(6))));
    }
}
