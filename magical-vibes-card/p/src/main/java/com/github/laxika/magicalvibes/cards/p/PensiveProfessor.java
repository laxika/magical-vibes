package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SOS", collectorNumber = "63")
@CardRegistration(set = "SOS", collectorNumber = "321")
public class PensiveProfessor extends Card {

    public PensiveProfessor() {
        // Increment is driven automatically by the Scryfall-loaded INCREMENT keyword; no effect needed.

        // Whenever one or more +1/+1 counters are put on Pensive Professor, draw a card.
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT, new DrawCardEffect(1));
    }
}
