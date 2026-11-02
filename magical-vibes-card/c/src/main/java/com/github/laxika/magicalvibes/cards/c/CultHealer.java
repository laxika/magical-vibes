package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "2")
public class CultHealer extends Card {

    public CultHealer() {
        GrantKeywordEffect gainLifelink = new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF);
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), gainLifelink);
        addTrigger(Triggers.youFullyUnlockRoom(), gainLifelink);
    }
}
