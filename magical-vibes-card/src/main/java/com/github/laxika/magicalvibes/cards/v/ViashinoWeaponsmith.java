package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "USG", collectorNumber = "226")
public class ViashinoWeaponsmith extends Card {

    public ViashinoWeaponsmith() {
        // Whenever this creature becomes blocked by a creature, this creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(2, 2));
    }
}
