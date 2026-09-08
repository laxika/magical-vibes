package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

@CardRegistration(set = "OGW", collectorNumber = "105")
public class CinderHellion extends Card {

    public CinderHellion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2, PlayerRelation.OPPONENT));
    }
}
