package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "225")
public class FlaringCinder extends Card {

    public FlaringCinder() {
        // When this creature enters and whenever you cast a spell with mana value 4 or greater (X counts
        // toward the mana value of a spell on the stack, CR 202.3e),
        // you may discard a card. If you do, draw a card.
        MayEffect rummage = new MayEffect(
                new DiscardAndDrawCardEffect(),
                "Discard a card to draw a card?"
        );
        addTrigger(Triggers.enters(ObjectRelation.THIS), rummage);
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(4, true)), rummage);
    }
}
