package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfChosenTypeToHandRestEffect;

@CardRegistration(set = "DIS", collectorNumber = "136")
public class VigeanIntuition extends Card {

    public VigeanIntuition() {
        addEffect(EffectSlot.SPELL,
                new RevealTopCardsOfChosenTypeToHandRestEffect(4, LookDestination.GRAVEYARD));
    }
}
