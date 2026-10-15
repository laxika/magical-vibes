package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "297")
public class HoppingAutomaton extends Card {

    public HoppingAutomaton() {
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new BoostSelfEffect(-1, -1), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{0}: This creature gets -1/-1 and gains flying until end of turn."));
    }
}
