package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardToHandOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "174")
public class PatientNaturalist extends Card {

    public PatientNaturalist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MillControllerAndReturnMilledCardToHandOrCreateTokenEffect(
                        3,
                        new CardTypePredicate(CardType.LAND),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
