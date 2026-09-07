package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnNControlledPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "PLC", collectorNumber = "20")
public class StormfrontRiders extends Card {

    public StormfrontRiders() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnNControlledPermanentsToHandEffect(2, new PermanentIsCreaturePredicate(), "creature"));
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_RETURNED_TO_HAND, CreateTokenEffect.whiteSoldier(1));
    }
}
