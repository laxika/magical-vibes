package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;

@CardRegistration(set = "BLB", collectorNumber = "9")
public class DawnsTruce extends Card {

    public DawnsTruce() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        addEffect(EffectSlot.SPELL, new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.HEXPROOF));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS)));
    }
}
