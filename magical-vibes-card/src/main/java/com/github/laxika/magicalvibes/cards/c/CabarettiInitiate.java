package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "137")
public class CabarettiInitiate extends Card {

    public CabarettiInitiate() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{R/W}",
                List.of(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)),
                "{2}{R/W}: This creature gains double strike until end of turn."));
    }
}
