package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "73")
public class KozileksShrieker extends Card {

    public KozileksShrieker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{C}",
                List.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
                ),
                "{C}: This creature gets +1/+0 and gains menace until end of turn."
        ));
    }
}
