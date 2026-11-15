package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "144")
public class Liability extends Card {

    public Liability() {
        addTrigger(Triggers.dies(Triggers.NONTOKEN, ObjectRelation.ANY),
                // "That player" is the graveyard's owner, the dying permanent's card's owner.
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_CARD_OWNER));
    }
}
