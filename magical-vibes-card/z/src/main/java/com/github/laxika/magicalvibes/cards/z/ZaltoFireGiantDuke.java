package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "171")
public class ZaltoFireGiantDuke extends Card {

    public ZaltoFireGiantDuke() {
        addTrigger(Triggers.dealtDamage(), new VentureIntoDungeonEffect());
    }
}
