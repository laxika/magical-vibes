package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "OPC2", collectorNumber = "15")
public class Gavony extends Card {

    public Gavony() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_OWN_CREATURES));
    }
}
