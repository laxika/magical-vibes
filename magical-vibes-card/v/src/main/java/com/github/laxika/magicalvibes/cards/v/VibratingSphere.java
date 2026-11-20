package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ICE", collectorNumber = "345")
@CardRegistration(set = "ME4", collectorNumber = "238")
public class VibratingSphere extends Card {

    public VibratingSphere() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(2, 0, Subjects.creaturesYouControl())));
        addStaticEffect(new ConditionalEffect(new NotControllerTurn(),
                new StaticBoostEffect(0, -2, Subjects.creaturesYouControl())));
    }
}
