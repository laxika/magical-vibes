package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EyeOfTheStormCastTriggerEffect;

@CardRegistration(set = "RAV", collectorNumber = "48")
public class EyeOfTheStorm extends Card {

    public EyeOfTheStorm() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new EyeOfTheStormCastTriggerEffect());
    }
}
