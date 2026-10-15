package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "157")
public class MinersBane extends Card {

    public MinersBane() {
        // {2}{R}: This creature gets +1/+0 and gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}", List.of(
                new BoostSelfEffect(1, 0),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{2}{R}: This creature gets +1/+0 and gains trample until end of turn."));
    }
}
