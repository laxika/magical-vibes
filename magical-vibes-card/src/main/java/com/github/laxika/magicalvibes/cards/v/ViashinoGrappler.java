package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "179")
public class ViashinoGrappler extends Card {

    public ViashinoGrappler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{G}: This creature gains trample until end of turn."
        ));
    }
}
