package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "44")
public class MerrowWavebreakers extends Card {

    public MerrowWavebreakers() {
        // {1}{U}, {Q}: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{1}{U}, {Q}: This creature gains flying until end of turn."
        ).withRequiresUntap());
    }
}
