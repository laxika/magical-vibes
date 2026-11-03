package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "61")
@CardRegistration(set = "ME4", collectorNumber = "51")
public class InTheEyeOfChaos extends Card {

    public InTheEyeOfChaos() {
        // Whenever a player casts an instant spell, counter it unless that player pays its mana value.
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.INSTANT)),
                new CounterUnlessPaysEffect(new TargetSpellManaValue()));
    }
}
