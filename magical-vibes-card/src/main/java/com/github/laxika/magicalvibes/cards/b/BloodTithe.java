package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeAndControllerGainsLifeLostEffect;

@CardRegistration(set = "M11", collectorNumber = "84")
public class BloodTithe extends Card {

    public BloodTithe() {
        addEffect(EffectSlot.SPELL, new EachOpponentLosesLifeAndControllerGainsLifeLostEffect(3));
    }
}
