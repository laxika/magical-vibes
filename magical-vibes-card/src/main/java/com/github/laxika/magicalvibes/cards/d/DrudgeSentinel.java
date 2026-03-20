package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapSelfEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "89")
public class DrudgeSentinel extends Card {

    public DrudgeSentinel() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new TapSelfEffect(), new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)),
                "{3}: Tap Drudge Sentinel. It gains indestructible until end of turn."
        ));
    }
}
