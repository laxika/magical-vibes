package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WWK", collectorNumber = "83")
public class GrotagThrasher extends Card {

    public GrotagThrasher() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK,
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
