package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "SOC", collectorNumber = "271")
@CardRegistration(set = "C21", collectorNumber = "62")
public class GuardianAugmenter extends Card {

    public GuardianAugmenter() {
        var commanderFilter = new PermanentIsCommanderPredicate();
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.creaturesYouControl(), commanderFilter));
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HEXPROOF, Subjects.otherPermanentsYouControl(), commanderFilter));
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HEXPROOF, Subjects.source(), commanderFilter));
    }
}
