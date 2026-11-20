package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "VOW", collectorNumber = "223")
@CardRegistration(set = "DBL", collectorNumber = "490")
public class SporebackWolf extends Card {

    public SporebackWolf() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(0, 2, Subjects.source())));
    }
}
