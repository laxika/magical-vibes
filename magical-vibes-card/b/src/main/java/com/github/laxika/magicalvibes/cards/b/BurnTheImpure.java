package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "MBS", collectorNumber = "59")
public class BurnTheImpure extends Card {

    public BurnTheImpure() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(3));
        addSpellEffect(new DealDamageToTargetControllerIfTargetHasKeywordEffect(3, Keyword.INFECT));
    }
}
