package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "STX", collectorNumber = "239")
public class StoneboundMentor extends Card {

    public StoneboundMentor() {
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, new ScryEffect(1));
    }
}
