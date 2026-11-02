package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "27")
public class VanguardSuppressor extends Card {

    public VanguardSuppressor() {
        // Squad {2}
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{2}")));

        // Create a token copy of Vanguard Suppressor for each time its squad cost was paid.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{2}")));

        // Whenever this creature deals combat damage to a player, draw a card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
    }
}
