package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleWhenExactlyTwoSameNameEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

@CardRegistration(set = "YWOE", collectorNumber = "27")
public class SyrJoshuaAndSyrSaxon extends Card {

    private static final String CARD_NAME = "Syr Joshua and Syr Saxon";

    public SyrJoshuaAndSyrSaxon() {
        addStaticEffect(new IgnoreLegendRuleWhenExactlyTwoSameNameEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))));
        addStaticEffect(new GrantKeywordEffect(
                Keyword.BATTLE_CRY,
                Subjects.otherCreaturesYouControl(),
                new PermanentNamedPredicate(CARD_NAME)));
    }
}
