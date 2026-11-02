package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EyeOfTheStormCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "48")
public class EyeOfTheStorm extends Card {

    public EyeOfTheStorm() {
        addTrigger(Triggers.playerCastsSpell(), new EyeOfTheStormCastTriggerEffect());
    }
}
