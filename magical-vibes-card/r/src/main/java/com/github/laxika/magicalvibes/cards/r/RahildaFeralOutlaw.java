package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileRandomNonlandCardFromDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class RahildaFeralOutlaw extends Card {

    public RahildaFeralOutlaw() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileRandomNonlandCardFromDamagedPlayerLibraryEffect(
                        RahildaWantedCutthroat.duringTurnAttackedWithWolfOrWerewolf()));
    }
}
