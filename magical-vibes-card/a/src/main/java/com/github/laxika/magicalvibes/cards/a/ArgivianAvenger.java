package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "232")
public class ArgivianAvenger extends Card {

    public ArgivianAvenger() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new BoostSelfEffect(-1, -1),
                        new GrantChosenKeywordEffect(
                                List.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.DEATHTOUCH, Keyword.HASTE),
                                GrantScope.SELF)),
                "{1}: Until end of turn, this creature gets -1/-1 and gains your choice of flying, vigilance, deathtouch, or haste."));
    }
}
