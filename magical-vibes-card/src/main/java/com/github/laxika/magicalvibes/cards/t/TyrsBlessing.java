package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class TyrsBlessing extends Card {

    public TyrsBlessing() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
    }
}
