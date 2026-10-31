package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "STX", collectorNumber = "75")
public class LeechFanatic extends Card {

    public LeechFanatic() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));
    }
}
