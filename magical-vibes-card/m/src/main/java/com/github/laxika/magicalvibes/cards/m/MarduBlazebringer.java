package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "115")
public class MarduBlazebringer extends Card {

    public MarduBlazebringer() {
        addTrigger(Triggers.attacks(), new SacrificeAtEndOfCombatEffect());
        addTrigger(Triggers.blocks(), new SacrificeAtEndOfCombatEffect());
    }
}
