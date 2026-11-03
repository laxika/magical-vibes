package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "17")
public class ExcavationElephant extends Card {

    public ExcavationElephant() {
        // Kicker {1}{W}
        addStaticEffect(new KickerEffect("{1}{W}"));

        // When this creature enters, if it was kicked, return target artifact card
        // from your graveyard to your hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                new ReturnTargetCardsFromGraveyardToHandEffect(
                        new CardTypePredicate(CardType.ARTIFACT), 1, null, false, true, 1,
                        false, Set.of(), false));
    }
}
