package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageControllerUnlessDiscardThenTapSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "337")
@CardRegistration(set = "SUM", collectorNumber = "266")
@CardRegistration(set = "ATQ", collectorNumber = "57")
@CardRegistration(set = "3ED", collectorNumber = "266")
public class MishrasWarMachine extends Card {

    public MishrasWarMachine() {
        // At the beginning of your upkeep, this creature deals 3 damage to you unless you discard a
        // card. If it deals damage to you this way, tap it.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DamageControllerUnlessDiscardThenTapSourceEffect(3));
    }
}
