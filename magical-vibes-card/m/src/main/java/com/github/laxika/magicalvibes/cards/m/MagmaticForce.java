package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AA4", collectorNumber = "20")
@CardRegistration(set = "CMD", collectorNumber = "128")
@CardRegistration(set = "SCD", collectorNumber = "150")
public class MagmaticForce extends Card {

    public MagmaticForce() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new DealDamageToAnyTargetEffect(3));
    }
}
