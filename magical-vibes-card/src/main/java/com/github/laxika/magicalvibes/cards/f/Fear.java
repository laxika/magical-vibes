package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "142")
public class Fear extends Card {

    public Fear() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FEAR, Scope.ENCHANTED_CREATURE));
    }
}
