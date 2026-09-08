package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOI", collectorNumber = "117")
public class HoundOfTheFarbogs extends Card {

    public HoundOfTheFarbogs() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)));
    }
}
