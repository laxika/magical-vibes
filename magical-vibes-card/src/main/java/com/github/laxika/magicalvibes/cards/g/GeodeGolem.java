package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect;

@CardRegistration(set = "CMM", collectorNumber = "386")
public class GeodeGolem extends Card {

    public GeodeGolem() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect());
    }
}
