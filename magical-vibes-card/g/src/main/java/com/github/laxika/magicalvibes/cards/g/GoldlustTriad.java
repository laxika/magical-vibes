package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "34")
@CardRegistration(set = "TDC", collectorNumber = "74")
@CardRegistration(set = "FDC", collectorNumber = "159")
public class GoldlustTriad extends Card {

    public GoldlustTriad() {
        addTrigger(Triggers.attacks(),
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1));
    }
}
