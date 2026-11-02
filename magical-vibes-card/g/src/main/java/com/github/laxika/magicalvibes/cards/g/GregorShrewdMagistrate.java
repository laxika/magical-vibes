package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "145")
@CardRegistration(set = "SLD", collectorNumber = "962")
@CardRegistration(set = "SLX", collectorNumber = "21")
public class GregorShrewdMagistrate extends Card {

    public GregorShrewdMagistrate() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(new SourcePower()));
    }
}
