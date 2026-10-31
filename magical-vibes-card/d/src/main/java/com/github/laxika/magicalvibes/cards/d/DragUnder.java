package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "57")
@CardRegistration(set = "W17", collectorNumber = "9")
@CardRegistration(set = "SIR", collectorNumber = "62")
public class DragUnder extends Card {

    public DragUnder() {
        // Return target creature to its owner's hand. Draw a card.
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.target());
        addSpellEffect(new DrawCardEffect(1));
    }
}
