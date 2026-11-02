package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "69")
@CardRegistration(set = "ZNC", collectorNumber = "31")
public class MilitaryIntelligence extends Card {

    public MilitaryIntelligence() {
        // Whenever you attack with two or more creatures, draw a card.
        addTrigger(Triggers.youAttack(),
                new ConditionalEffect(new MinimumAttackers(2), new DrawCardEffect()));
    }
}
