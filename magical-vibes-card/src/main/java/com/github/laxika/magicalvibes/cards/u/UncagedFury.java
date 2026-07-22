package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "INR", collectorNumber = "177")
public class UncagedFury extends Card {

    public UncagedFury() {
        // Target creature gets +1/+1 and gains double strike until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
    }
}
