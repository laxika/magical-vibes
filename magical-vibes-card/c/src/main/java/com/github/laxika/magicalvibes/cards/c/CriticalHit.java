package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "137")
public class CriticalHit extends Card {

    public CriticalHit() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_ROLLS_NATURAL_20,
                new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
