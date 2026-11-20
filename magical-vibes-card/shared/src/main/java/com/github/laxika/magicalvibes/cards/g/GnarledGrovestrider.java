package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class GnarledGrovestrider extends Card {

    public GnarledGrovestrider() {
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.otherCreaturesYouControl()));
    }
}
