package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class GitaxianMindstinger extends Card {

    public GitaxianMindstinger() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrBattle(),
                new DrawCardEffect());
    }
}
