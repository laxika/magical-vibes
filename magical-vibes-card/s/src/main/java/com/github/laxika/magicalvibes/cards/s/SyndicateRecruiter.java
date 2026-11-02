package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DistinctManaValuesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "30")
public class SyndicateRecruiter extends Card {

    public SyndicateRecruiter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new MillEffect(4, MillRecipient.CONTROLLER),
                new ConditionalEffect(
                        new DistinctManaValuesAmongCardsInGraveyardAtLeast(5),
                        new ConjureCardToHandEffect("SNC", "76"))));
    }
}
