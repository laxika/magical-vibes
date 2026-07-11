package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "9ED", collectorNumber = "99")
@CardRegistration(set = "P02", collectorNumber = "46")
public class SleightOfHand extends Card {

    public SleightOfHand() {
        // Look at the top two cards of your library. Put one of them into your hand and the
        // other on the bottom of your library.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2)));
    }
}
