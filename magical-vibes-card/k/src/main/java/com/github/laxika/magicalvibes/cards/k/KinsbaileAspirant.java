package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;

@CardRegistration(set = "ECL", collectorNumber = "21")
public class KinsbaileAspirant extends Card {

    public KinsbaileAspirant() {
        // As an additional cost to cast this spell, behold a Kithkin or pay {2}.
        addStaticEffect(new IncreaseOwnCastCostUnlessRevealSubtypeEffect(2, CardSubtype.KITHKIN, true));

        // Whenever another creature you control enters, this creature gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 1));
    }
}
