package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class RooftopSaboteurs extends Card {

    public RooftopSaboteurs() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrBattle(),
                new DrawCardEffect());
    }
}
