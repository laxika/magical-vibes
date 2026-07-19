package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "43")
public class Dreadwing extends Card {

    public Dreadwing() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}{R}",
                List.of(new BoostSelfEffect(3, 0), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{1}{U}{R}: This creature gets +3/+0 and gains flying until end of turn."));
    }
}
