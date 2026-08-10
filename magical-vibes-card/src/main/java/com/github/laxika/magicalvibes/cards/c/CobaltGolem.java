package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "157")
public class CobaltGolem extends Card {

    public CobaltGolem() {
        // {1}{U}: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{1}{U}: This creature gains flying until end of turn."));
    }
}
