package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "386")
@CardRegistration(set = "C18", collectorNumber = "56")
public class GeodeGolem extends Card {

    public GeodeGolem() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect());
    }
}
