package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.GraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "77")
public class TheMasterOfLakeTown extends Card {

    public TheMasterOfLakeTown() {
        addTrigger(List.of(Triggers.opponentLosesLife(), Triggers.youLoseLife()),
                new MillEffect(new EventValue(), MillRecipient.TRIGGERING_PLAYER));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect(new GraveyardsAtLeast(7)));
    }
}
