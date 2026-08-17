package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "ROE", collectorNumber = "85")
public class SeaGateOracle extends Card {

    public SeaGateOracle() {
        // When this creature enters, look at the top two cards of your library. Put one of them
        // into your hand and the other on the bottom of your library.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2)));
    }
}
