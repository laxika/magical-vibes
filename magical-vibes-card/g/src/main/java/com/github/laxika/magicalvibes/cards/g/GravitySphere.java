package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "LEG", collectorNumber = "151")
public class GravitySphere extends Card {

    public GravitySphere() {
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, Subjects.allCreatures()));
    }
}
