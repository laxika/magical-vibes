package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "HML", collectorNumber = "34")
@CardRegistration(set = "ME4", collectorNumber = "53")
public class MysticDecree extends Card {

    public MysticDecree() {
        // All creatures lose flying and islandwalk.
        // Two continuous layer-6 keyword removals over every creature on the battlefield.
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, Subjects.allCreatures()));
        addStaticEffect(new RemoveKeywordEffect(Keyword.ISLANDWALK, Subjects.allCreatures()));
    }
}
