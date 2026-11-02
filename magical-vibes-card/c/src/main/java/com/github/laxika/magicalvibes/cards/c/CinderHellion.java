package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "105")
public class CinderHellion extends Card {

    public CinderHellion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2, PlayerRelation.OPPONENT));
    }
}
