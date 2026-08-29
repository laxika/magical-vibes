package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "PLS", collectorNumber = "92")
public class SkyshroudBlessing extends Card {

    public SkyshroudBlessing() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ALL_LANDS));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
