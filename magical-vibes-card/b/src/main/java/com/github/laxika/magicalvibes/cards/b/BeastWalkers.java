package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "6")
public class BeastWalkers extends Card {

    public BeastWalkers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new GrantKeywordEffect(Keyword.BANDING, GrantScope.SELF)),
                "{G}: Beast Walkers gains banding until end of turn."
        ));
    }
}
