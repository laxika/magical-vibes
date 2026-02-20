package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFlyingOrSubtypeEffect;

@CardRegistration(set = "10E", collectorNumber = "259")
public class ElvenRiders extends Card {

    public ElvenRiders() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFlyingOrSubtypeEffect(CardSubtype.WALL));
    }
}
