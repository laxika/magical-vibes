package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "BNG", collectorNumber = "54")
public class ThassasRebuff extends Card {

    public ThassasRebuff() {
        // Counter target spell unless its controller pays {X}, where X is your devotion to blue.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLUE)));
    }
}
