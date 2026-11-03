package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "51")
@CardRegistration(set = "LCC", collectorNumber = "170")
public class SeafloorOracle extends Card {

    public SeafloorOracle() {
        // Whenever a Merfolk you control deals combat damage to a player, draw a card.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                new DrawCardEffect(1));
    }
}
