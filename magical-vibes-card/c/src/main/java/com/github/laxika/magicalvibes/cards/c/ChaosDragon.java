package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20ForEachPlayerAndRestrictSourceAttacksEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "30")
public class ChaosDragon extends Card {

    public ChaosDragon() {
        addStaticEffect(new MustAttackEffect());
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new RollD20ForEachPlayerAndRestrictSourceAttacksEffect());
    }
}
