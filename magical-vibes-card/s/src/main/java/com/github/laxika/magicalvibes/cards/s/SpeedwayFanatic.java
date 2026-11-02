package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "132")
@CardRegistration(set = "KLR", collectorNumber = "147")
public class SpeedwayFanatic extends Card {

    public SpeedwayFanatic() {
        // Whenever this creature crews a Vehicle, that Vehicle gains haste until end of turn.
        addTrigger(Triggers.crewsVehicle(),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT));
    }
}
