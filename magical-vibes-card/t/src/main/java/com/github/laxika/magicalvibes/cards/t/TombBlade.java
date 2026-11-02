package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "64")
public class TombBlade extends Card {

    public TombBlade() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                ForcedCostOrElseEffect.enchantedControllerMayPay(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "a creature"),
                        List.of(new LoseLifeEffect(
                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER),
                                LoseLifeRecipient.TARGET_PLAYER))));

        addUnearth("{6}{B}{B}");
    }
}
