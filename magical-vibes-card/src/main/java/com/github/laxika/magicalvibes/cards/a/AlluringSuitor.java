package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DeadlyDancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "VOW", collectorNumber = "141")
public class AlluringSuitor extends Card {

    public AlluringSuitor() {
        setBackFaceCard(new DeadlyDancer());

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new ExactlyAttackers(2), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "DeadlyDancer";
    }
}
