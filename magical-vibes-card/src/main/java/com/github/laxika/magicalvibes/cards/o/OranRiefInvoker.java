package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "182")
public class OranRiefInvoker extends Card {

    public OranRiefInvoker() {
        addActivatedAbility(new ActivatedAbility(false, "{8}",
                List.of(new BoostSelfEffect(5, 5), new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{8}: This creature gets +5/+5 and gains trample until end of turn."));
    }
}
