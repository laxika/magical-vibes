package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "179")
public class DireWolfProwler extends Card {

    public DireWolfProwler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new BoostSelfEffect(2, 2),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)
                ),
                "{1}{G}: This creature gets +2/+2 and gains haste until end of turn. Activate only once each turn.",
                1
        ));
    }
}
