package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "101")
public class PyreheartWolf extends Card {

    public PyreheartWolf() {
        addTrigger(Triggers.attacks(), new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES));
    }
}
