package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "134")
@CardRegistration(set = "KLR", collectorNumber = "204")
public class RenegadeWheelsmith extends Card {

    public RenegadeWheelsmith() {
        // Whenever this creature becomes tapped, target creature can't block this turn.
        target(TargetFilters.creature()).addTrigger(Triggers.becomesTapped(),
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
