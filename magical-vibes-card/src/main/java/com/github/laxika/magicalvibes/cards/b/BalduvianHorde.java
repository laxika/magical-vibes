package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "167")
@CardRegistration(set = "ALL", collectorNumber = "65")
@CardRegistration(set = "BTD", collectorNumber = "34")
public class BalduvianHorde extends Card {

    public BalduvianHorde() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeUnlessDiscardCardTypeEffect(null, true));
    }
}
