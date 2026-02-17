package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

public class HighwayRobber extends Card {

    public HighwayRobber() {
        setNeedsTarget(true);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(2, 2));
    }
}
