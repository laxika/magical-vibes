package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastFromTargetPlayerHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PC2", collectorNumber = "105")
@CardRegistration(set = "PCA", collectorNumber = "105")
@CardRegistration(set = "OTC", collectorNumber = "241")
@CardRegistration(set = "C18", collectorNumber = "191")
public class SilentBladeOni extends Card {

    public SilentBladeOni() {
        addNinjutsu("{4}{U}{B}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                MayCastFromTargetPlayerHandWithoutPayingManaCostEffect.damagedPlayer());
    }
}
