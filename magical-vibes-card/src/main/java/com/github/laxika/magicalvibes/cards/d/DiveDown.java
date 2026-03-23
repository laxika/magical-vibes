package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "XLN", collectorNumber = "53")
public class DiveDown extends Card {

    public DiveDown() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(0, 3));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET));
    }
}
