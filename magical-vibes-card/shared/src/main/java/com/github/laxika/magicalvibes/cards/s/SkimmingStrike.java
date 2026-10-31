package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class SkimmingStrike extends Card {

    public SkimmingStrike() {
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET));
        addSpellEffect(new DrawCardEffect(1));
    }
}
