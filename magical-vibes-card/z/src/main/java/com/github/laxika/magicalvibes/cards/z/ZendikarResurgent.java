package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "147")
@CardRegistration(set = "C17", collectorNumber = "160")
public class ZendikarResurgent extends Card {

    public ZendikarResurgent() {
        // Whenever you tap a land for mana, add one mana of any type that land produced.
        addTrigger(Triggers.playerTapsLandForMana(),
                new AddOneOfEachManaTypeProducedByLandEffect(true));

        // Whenever you cast a creature spell, draw a card.
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)), new DrawCardEffect(1));
    }
}
