package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "ELD", collectorNumber = "50")
public class IntoTheStory extends Card {

    public IntoTheStory() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentGraveyardAtLeast(7), new ReduceOwnCastCostEffect(new Fixed(3))));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
    }
}
