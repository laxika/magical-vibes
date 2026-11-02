package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "143")
public class GrotagNightRunner extends Card {

    public GrotagNightRunner() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
