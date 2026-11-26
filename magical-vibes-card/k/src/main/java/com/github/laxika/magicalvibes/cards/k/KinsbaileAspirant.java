package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "21")
public class KinsbaileAspirant extends Card {

    public KinsbaileAspirant() {
        // As an additional cost to cast this spell, behold a Kithkin or pay {2}.
        addStaticEffect(new IncreaseOwnCastCostUnlessRevealSubtypeEffect(2, CardSubtype.KITHKIN, true));

        // Whenever another creature you control enters, this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), BoostEffect.self(1, 1));
    }
}
