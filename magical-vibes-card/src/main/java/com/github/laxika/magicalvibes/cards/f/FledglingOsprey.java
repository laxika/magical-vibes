package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "UDS", collectorNumber = "33")
public class FledglingOsprey extends Card {

    public FledglingOsprey() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Enchanted(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
