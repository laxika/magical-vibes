package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class BelenonWarAnthem extends Card {

    public BelenonWarAnthem() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl()));
    }
}
