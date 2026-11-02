package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "16")
@CardRegistration(set = "KLR", collectorNumber = "20")
public class GearshiftAce extends Card {

    public GearshiftAce() {
        // Whenever this creature crews a Vehicle, that Vehicle gains first strike until end of turn.
        addTrigger(Triggers.crewsVehicle(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TRIGGERING_PERMANENT));
    }
}
