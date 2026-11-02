package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "187")
public class MarduRoughrider extends Card {

    public MarduRoughrider() {
        // Whenever this creature attacks, target creature can't block this turn.
        target(TargetFilters.creature()).addTrigger(Triggers.attacks(),
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
