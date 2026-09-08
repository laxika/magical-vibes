package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "192")
public class PestilentWolf extends Card {

    public PestilentWolf() {
        // {2}{G}: This creature gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{2}{G}: This creature gains deathtouch until end of turn."));
    }
}
