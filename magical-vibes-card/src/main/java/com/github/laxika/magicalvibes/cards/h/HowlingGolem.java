package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "DOM", collectorNumber = "218")
public class HowlingGolem extends Card {

    public HowlingGolem() {
        // Whenever this creature attacks or blocks, each player draws a card.
        addEffect(EffectSlot.ON_ATTACK, new EachPlayerDrawsCardEffect(1));
        addEffect(EffectSlot.ON_BLOCK, new EachPlayerDrawsCardEffect(1));
    }
}
