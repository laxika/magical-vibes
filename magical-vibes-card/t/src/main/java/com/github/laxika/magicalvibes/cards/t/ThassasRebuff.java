package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "BNG", collectorNumber = "54")
public class ThassasRebuff extends Card {

    public ThassasRebuff() {
        // Counter target spell unless its controller pays {X}, where X is your devotion to blue.
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLUE)));
    }
}
