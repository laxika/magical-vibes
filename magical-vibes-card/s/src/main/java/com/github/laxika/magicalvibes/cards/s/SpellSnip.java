package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;


@CardRegistration(set = "ALA", collectorNumber = "57")
public class SpellSnip extends Card {

    public SpellSnip() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(1));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
