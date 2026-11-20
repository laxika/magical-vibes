package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LRW", collectorNumber = "98")
public class ZephyrNet extends Card {

    public ZephyrNet() {
        // Enchant creature
        target(TargetFilters.creature());
        addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()));
    }
}
