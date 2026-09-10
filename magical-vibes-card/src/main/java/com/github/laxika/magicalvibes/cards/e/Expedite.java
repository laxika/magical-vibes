package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "108")
public class Expedite extends Card {

    public Expedite() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
