package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToLandDifferenceEffect;

@CardRegistration(set = "NCC", collectorNumber = "90")
@CardRegistration(set = "NCC", collectorNumber = "98")
public class SpitefulRepossession extends Card {

    public SpitefulRepossession() {
        addSpellEffect(new DealDamageToEachOpponentEqualToLandDifferenceEffect());
        addSpellEffect(CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
