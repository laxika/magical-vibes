package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "DGM", collectorNumber = "4")
public class MazeSentinel extends Card {

    public MazeSentinel() {
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.creaturesYouControl(),
                new PermanentIsMulticoloredPredicate()));
    }
}
