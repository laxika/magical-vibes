package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DeadlyDancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "141")
@CardRegistration(set = "DBL", collectorNumber = "408")
public class AlluringSuitor extends Card {

    public AlluringSuitor() {
        setBackFaceCard(new DeadlyDancer());

        addTrigger(Triggers.youAttack(),
                new ConditionalEffect(new ExactlyAttackers(2), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "DeadlyDancer";
    }
}
