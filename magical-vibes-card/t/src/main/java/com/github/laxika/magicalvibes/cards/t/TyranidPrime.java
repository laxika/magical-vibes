package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "40K", collectorNumber = "145")
public class TyranidPrime extends Card {

    public TyranidPrime() {
        addStaticEffect(new GrantKeywordEffect(Keyword.EVOLVE, GrantScope.OWN_CREATURES));
    }
}
