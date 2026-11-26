package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "CON", collectorNumber = "22")
public class ConstrictingTendrils extends Card {

    public ConstrictingTendrils() {
        // Target creature gets -3/-0 until end of turn.
        addSpellEffect(BoostEffect.target(-3, 0));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
