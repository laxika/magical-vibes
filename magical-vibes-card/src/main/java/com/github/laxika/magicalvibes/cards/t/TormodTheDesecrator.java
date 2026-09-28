package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SLD", collectorNumber = "855")
public class TormodTheDesecrator extends Card {

    public TormodTheDesecrator() {
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                CreateTokenEffect.blackZombie(1).withTapped(true));
    }
}
