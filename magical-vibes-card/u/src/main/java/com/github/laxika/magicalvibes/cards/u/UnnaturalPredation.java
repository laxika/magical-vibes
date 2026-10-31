package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MBS", collectorNumber = "93")
public class UnnaturalPredation extends Card {

    public UnnaturalPredation() {
        addSpellEffect(new BoostTargetCreatureEffect(1, 1));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
