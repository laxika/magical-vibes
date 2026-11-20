package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "273")
@CardRegistration(set = "DDG", collectorNumber = "32")
public class SpidersilkArmor extends Card {

    public SpidersilkArmor() {
        addStaticEffect(new StaticBoostEffect(0, 1, Set.of(Keyword.REACH), Subjects.creaturesYouControl()));
    }
}
