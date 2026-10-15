package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect;

@CardRegistration(set = "UDS", collectorNumber = "54")
public class BubblingMuck extends Card {

    public BubblingMuck() {
        addEffect(EffectSlot.SPELL,
                new LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect(CardSubtype.SWAMP, ManaColor.BLACK));
    }
}
