package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "108")
public class InTheWebOfWar extends Card {

    public InTheWebOfWar() {
        // Whenever a creature you control enters, it gets +2/+0 and gains haste until end of turn.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new BoostEnteringCreatureEffect(2, 0, Set.of(Keyword.HASTE)));
    }
}
