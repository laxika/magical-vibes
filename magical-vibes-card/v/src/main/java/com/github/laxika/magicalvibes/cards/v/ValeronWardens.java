package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "203")
public class ValeronWardens extends Card {

    public ValeronWardens() {
        // Renown 2
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned())),
                new RenownEffect(2));

        // Whenever a creature you control becomes renowned, draw a card.
        addTrigger(Triggers.creatureYouControlBecomesRenowned(), new DrawCardEffect(1));
    }
}
