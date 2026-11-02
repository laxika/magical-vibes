package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "47")
public class RavenGuildMaster extends Card {

    public RavenGuildMaster() {
        addMorph("{2}{U}{U}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardsOfTargetPlayerLibraryEffect(10));
    }
}
