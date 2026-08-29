package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "VIS", collectorNumber = "34")
@CardRegistration(set = "BTD", collectorNumber = "10")
public class Impulse extends Card {

    public Impulse() {
        // Look at the top four cards of your library. Put one of them into your hand and the rest
        // on the bottom of your library in any order.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(4)));
    }
}
