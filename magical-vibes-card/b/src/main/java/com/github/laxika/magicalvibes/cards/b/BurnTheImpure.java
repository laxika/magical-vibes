package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "MBS", collectorNumber = "59")
public class BurnTheImpure extends Card {

    public BurnTheImpure() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetControllerIfTargetHasKeywordEffect(3, Keyword.INFECT));
    }
}
