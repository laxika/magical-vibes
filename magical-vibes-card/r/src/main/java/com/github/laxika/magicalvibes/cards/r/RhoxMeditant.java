package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "16")
public class RhoxMeditant extends Card {

    public RhoxMeditant() {
        // When this creature enters, if you control a green permanent, draw a card.
        // Intervening-if handled by ConditionalEffect on the ETB slot (CR 603.4).
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.GREEN))),
                new DrawCardEffect(1)));
    }
}
