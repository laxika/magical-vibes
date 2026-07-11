package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "POR", collectorNumber = "66")
public class Prosperity extends Card {

    public Prosperity() {
        // Each player draws X cards.
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(new XValue()));
    }
}
