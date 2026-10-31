package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOM", collectorNumber = "80")
public class TaintedStrike extends Card {

    public TaintedStrike() {
        addSpellEffect(new BoostTargetCreatureEffect(1, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.INFECT, GrantScope.TARGET));
    }
}
