package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

@CardRegistration(set = "10E", collectorNumber = "179")
public class SoulFeast extends Card {

    public SoulFeast() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(4, 4));
    }
}
