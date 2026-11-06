package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfDamagedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "185")
public class CragSaurian extends Card {

    public CragSaurian() {
        addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,
                new DamageSourceControllerGainsControlOfDamagedPermanentEffect());
    }
}
