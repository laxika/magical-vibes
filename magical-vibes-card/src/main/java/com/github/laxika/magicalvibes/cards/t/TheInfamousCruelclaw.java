package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastByDiscardEffect;

@CardRegistration(set = "BLB", collectorNumber = "219")
public class TheInfamousCruelclaw extends Card {

    public TheInfamousCruelclaw() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopUntilNonlandMayCastByDiscardEffect());
    }
}
