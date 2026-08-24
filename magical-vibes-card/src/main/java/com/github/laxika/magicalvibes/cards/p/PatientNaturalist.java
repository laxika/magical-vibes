package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardToHandOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "174")
public class PatientNaturalist extends Card {

    public PatientNaturalist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillControllerAndReturnMilledCardToHandOrCreateTokenEffect(
                        3,
                        new CardTypePredicate(CardType.LAND),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
