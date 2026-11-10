package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2498")
@CardRegistration(set = "AFC", collectorNumber = "45")
public class DragonbornChampion extends Card {

    public DragonbornChampion() {
        // Whenever a source you control deals 5 or more damage to a player, draw a card.
        addTrigger(Triggers.sourceYouControlDealsDamageToPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(5)),
                new DrawCardEffect(1));
    }
}
