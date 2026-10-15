package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToLandDifferenceEffect;

@CardRegistration(set = "NCC", collectorNumber = "90")
@CardRegistration(set = "NCC", collectorNumber = "98")
public class SpitefulRepossession extends Card {

    public SpitefulRepossession() {
        addEffect(EffectSlot.SPELL, new DealDamageToEachOpponentEqualToLandDifferenceEffect());
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
