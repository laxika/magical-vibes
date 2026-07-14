package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "EVE", collectorNumber = "135")
public class DoubleCleave extends Card {

    public DoubleCleave() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
    }
}
