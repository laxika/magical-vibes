package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "BOK", collectorNumber = "92")
public class AkkiRaider extends Card {

    public AkkiRaider() {
        // Whenever a land is put into a graveyard from the battlefield, this creature gets +1/+0 until end of turn.
        addEffect(EffectSlot.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new BoostSelfEffect(1, 0));
    }
}
