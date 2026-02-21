package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "40")
public class SerrasEmbrace extends Card {

    public SerrasEmbrace() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new BoostEnchantedCreatureEffect(2, 2));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, Scope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, Scope.ENCHANTED_CREATURE));
    }
}
