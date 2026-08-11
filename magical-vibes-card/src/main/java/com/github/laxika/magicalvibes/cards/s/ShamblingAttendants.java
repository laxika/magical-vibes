package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "KTK", collectorNumber = "89")
public class ShamblingAttendants extends Card {

    public ShamblingAttendants() {
        addEffect(EffectSlot.SPELL, new DelveCost());
    }
}
