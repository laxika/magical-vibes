package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "193")
public class OutlandColossus extends Card {

    public OutlandColossus() {
        // Renown 6
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(6));
        // This creature can't be blocked by more than one creature.
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
