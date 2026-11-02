package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "85")
@CardRegistration(set = "6ED", collectorNumber = "69")
@CardRegistration(set = "WTH", collectorNumber = "40")
@CardRegistration(set = "BTD", collectorNumber = "7")
public class FogElemental extends Card {

    public FogElemental() {
        addTrigger(Triggers.attacks(), new SacrificeAtEndOfCombatEffect());
        addTrigger(Triggers.blocks(), new SacrificeAtEndOfCombatEffect());
    }
}
