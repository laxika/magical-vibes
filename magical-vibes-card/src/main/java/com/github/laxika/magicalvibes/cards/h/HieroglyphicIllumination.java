package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;


@CardRegistration(set = "AKH", collectorNumber = "57")
public class HieroglyphicIllumination extends Card {

    public HieroglyphicIllumination() {
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));

        // Cycling {U} ({U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{U}");
    }
}
