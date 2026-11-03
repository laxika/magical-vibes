package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredOrCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "187")
public class BreathlessKnight extends Card {

    public BreathlessKnight() {
        // Whenever this creature or another creature you control enters from a graveyard or is cast
        // from a graveyard, put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentEnteredOrCastFromZonePredicate(Zone.GRAVEYARD))),
                        ObjectRelation.YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
