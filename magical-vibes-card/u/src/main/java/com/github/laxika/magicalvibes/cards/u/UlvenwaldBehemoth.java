package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

public class UlvenwaldBehemoth extends Card {

    public UlvenwaldBehemoth() {
        addStaticEffect(new StaticBoostEffect(
                1,
                1,
                Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                Subjects.otherCreaturesYouControl()
        ));
    }
}
