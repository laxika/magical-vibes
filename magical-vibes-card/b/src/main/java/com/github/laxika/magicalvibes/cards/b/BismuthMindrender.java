package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M3C", collectorNumber = "51")
public class BismuthMindrender extends Card {

    public BismuthMindrender() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect());
    }
}
