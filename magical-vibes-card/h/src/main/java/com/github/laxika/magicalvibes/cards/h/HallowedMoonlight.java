package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUncastEnteringCreaturesEffect;

@CardRegistration(set = "ORI", collectorNumber = "16")
@CardRegistration(set = "PIO", collectorNumber = "359")
public class HallowedMoonlight extends Card {

    public HallowedMoonlight() {
        // Until end of turn, if a creature would enter and it wasn't cast, exile it instead.
        addSpellEffect(new ExileUncastEnteringCreaturesEffect());
        // Draw a card.
        addSpellEffect(new DrawCardEffect(1));
    }
}
