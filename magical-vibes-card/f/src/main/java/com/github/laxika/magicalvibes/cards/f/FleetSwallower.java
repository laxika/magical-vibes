package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "57")
@CardRegistration(set = "SLD", collectorNumber = "316")
@CardRegistration(set = "SLD", collectorNumber = "760")
public class FleetSwallower extends Card {

    public FleetSwallower() {
        // Whenever Fleet Swallower attacks, target player mills half their library, rounded up.
        addTrigger(Triggers.attacks(), new MillHalfLibraryEffect(true));
    }
}
