package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "115")
public class MarduBlazebringer extends Card {

    public MarduBlazebringer() {
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), new SacrificeAtEndOfCombatEffect());
    }
}
