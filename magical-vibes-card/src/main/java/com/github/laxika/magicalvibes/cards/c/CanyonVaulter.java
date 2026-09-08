package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DFT", collectorNumber = "8")
public class CanyonVaulter extends Card {

    public CanyonVaulter() {
        addEffect(EffectSlot.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
