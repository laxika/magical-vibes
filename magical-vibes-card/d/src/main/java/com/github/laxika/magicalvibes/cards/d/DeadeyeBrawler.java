package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "155")
public class DeadeyeBrawler extends Card {

    public DeadeyeBrawler() {
        addStaticEffect(new AscendEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ConditionalEffect(
                new ControllerHasCityBlessing(), new DrawCardEffect()));
    }
}
