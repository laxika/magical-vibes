package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMD", collectorNumber = "10")
public class CelestialForce extends Card {

    public CelestialForce() {
        // At the beginning of each upkeep, you gain 3 life.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new GainLifeEffect(3));
    }
}
