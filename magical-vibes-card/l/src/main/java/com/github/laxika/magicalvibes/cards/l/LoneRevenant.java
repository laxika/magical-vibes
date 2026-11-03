package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "64")
@CardRegistration(set = "C15", collectorNumber = "96")
public class LoneRevenant extends Card {

    public LoneRevenant() {
        // Whenever this creature deals combat damage to a player, if you control no other creatures,
        // look at the top four cards of your library. Put one of them into your hand and the rest on
        // the bottom of your library in any order.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new NoOtherPermanent(new PermanentIsCreaturePredicate())),
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(4)));
    }
}
