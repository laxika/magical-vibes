package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MMQ", collectorNumber = "142")
public class Intimidation extends Card {

    public Intimidation() {
        addStaticEffect(new GrantKeywordEffect(Keyword.FEAR, GrantScope.ALL_OWN_CREATURES));
    }
}
