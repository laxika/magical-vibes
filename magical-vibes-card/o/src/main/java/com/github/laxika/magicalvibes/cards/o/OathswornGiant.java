package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "27")
public class OathswornGiant extends Card {

    public OathswornGiant() {
        addStaticEffect(new StaticBoostEffect(0, 2, Set.of(Keyword.VIGILANCE), Subjects.otherCreaturesYouControl()));
    }
}
