package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;

@CardRegistration(set = "GPT", collectorNumber = "13")
public class OrderOfTheStars extends Card {

    public OrderOfTheStars() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProtectionFromChosenColorEffect());
    }
}
