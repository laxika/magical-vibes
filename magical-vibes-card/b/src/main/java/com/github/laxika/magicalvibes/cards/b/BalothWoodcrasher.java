package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "157")
@CardRegistration(set = "C13", collectorNumber = "136")
@CardRegistration(set = "CMD", collectorNumber = "143")
@CardRegistration(set = "C18", collectorNumber = "130")
public class BalothWoodcrasher extends Card {

    public BalothWoodcrasher() {
        // Landfall — Whenever a land you control enters, this creature gets +4/+4 and gains trample until end of turn.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new BoostSelfEffect(4, 4),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
    }
}
