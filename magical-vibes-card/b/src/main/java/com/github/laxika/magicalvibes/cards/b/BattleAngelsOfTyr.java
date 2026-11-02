package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BattleAngelsOfTyrEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "875")
public class BattleAngelsOfTyr extends Card {

    public BattleAngelsOfTyr() {
        addTrigger(Triggers.attacks(),
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new BattleAngelsOfTyrEffect());
    }
}
