package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class DuskSight extends Card {

    public DuskSight() {
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
