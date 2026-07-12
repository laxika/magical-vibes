package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

@CardRegistration(set = "10E", collectorNumber = "179")
@CardRegistration(set = "9ED", collectorNumber = "164")
@CardRegistration(set = "8ED", collectorNumber = "165")
public class SoulFeast extends Card {

    public SoulFeast() {
        addEffect(EffectSlot.SPELL, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(4, 4));
    }
}
