package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "20")
public class DreamThief extends Card {

    public DreamThief() {
        // When this creature enters, draw a card if you've cast another blue spell this turn.
        // The resolving-spell exclusion drops Dream Thief's own creature spell, so "another".
        addTrigger(Triggers.enters(ObjectRelation.THIS), ConditionalEffect.unless(
                new ControllerCastAnotherSpellThisTurn(new CardColorPredicate(CardColor.BLUE)),
                new DrawCardEffect(1)));
    }
}
