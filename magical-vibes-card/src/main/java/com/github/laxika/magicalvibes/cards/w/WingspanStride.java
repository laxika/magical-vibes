package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "66")
public class WingspanStride extends Card {

    public WingspanStride() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}", List.of(ReturnToHandEffect.self()),
                "{2}{U}: Return this Aura to its owner's hand."));
    }
}
