package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "WAR", collectorNumber = "215")
public class RubblebeltRioters extends Card {

    public RubblebeltRioters() {
        // Whenever this creature attacks, it gets +X/+0 until end of turn, where X is the
        // greatest power among creatures you control.
        addEffect(EffectSlot.ON_ATTACK,
                new BoostSelfEffect(new GreatestPowerAmongControlled(), new Fixed(0)));
    }
}
