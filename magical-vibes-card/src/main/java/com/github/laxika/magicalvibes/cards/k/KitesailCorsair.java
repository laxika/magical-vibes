package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "RIX", collectorNumber = "41")
public class KitesailCorsair extends Card {

    public KitesailCorsair() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsAttacking(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
