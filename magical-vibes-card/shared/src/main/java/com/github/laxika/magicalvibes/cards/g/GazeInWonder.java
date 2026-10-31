package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class GazeInWonder extends Card {

    public GazeInWonder() {
        target(TargetFilters.creature(), 1, 2)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
