package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "305")
public class IgneousGolem extends Card {

    public IgneousGolem() {
        // {2}: This creature gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{2}: This creature gains trample until end of turn."));
    }
}
