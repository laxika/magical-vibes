package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M19", collectorNumber = "302")
@CardRegistration(set = "M20", collectorNumber = "337")
public class AggressiveMammoth extends Card {

    public AggressiveMammoth() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
    }
}
