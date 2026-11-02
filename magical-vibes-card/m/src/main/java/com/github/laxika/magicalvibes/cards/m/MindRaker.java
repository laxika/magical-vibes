package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "95")
public class MindRaker extends Card {

    public MindRaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new PutOpponentOwnedExiledCardIntoGraveyardEffect(),
                new ConditionalEffect(new EventValueAtLeast(1),
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT))));
    }
}
