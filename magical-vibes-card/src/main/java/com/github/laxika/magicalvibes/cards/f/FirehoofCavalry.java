package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "11")
public class FirehoofCavalry extends Card {

    public FirehoofCavalry() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{R}",
                List.of(
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{3}{R}: This creature gets +2/+0 and gains trample until end of turn."));
    }
}
