package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class BeatAPath extends Card {

    public BeatAPath() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
