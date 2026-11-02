package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "302")
@CardRegistration(set = "ANB", collectorNumber = "2")
public class AngelicGuardian extends Card {

    public AngelicGuardian() {
        addTrigger(Triggers.youAttack(),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGETS));
    }
}
