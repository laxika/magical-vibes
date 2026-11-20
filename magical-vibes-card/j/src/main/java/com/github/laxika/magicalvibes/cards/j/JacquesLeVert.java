package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "232")
@CardRegistration(set = "ME1", collectorNumber = "147")
public class JacquesLeVert extends Card {

    public JacquesLeVert() {
        addStaticEffect(new StaticBoostEffect(0, 2, Subjects.creaturesYouControl(),
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
