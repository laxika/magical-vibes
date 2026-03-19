package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DOM", collectorNumber = "2")
public class AdamantWill extends Card {

    public AdamantWill() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
    }
}
