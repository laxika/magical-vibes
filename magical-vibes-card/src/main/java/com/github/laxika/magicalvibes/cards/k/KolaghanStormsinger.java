package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "145")
public class KolaghanStormsinger extends Card {

    public KolaghanStormsinger() {
        addMorph("{R}");
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
