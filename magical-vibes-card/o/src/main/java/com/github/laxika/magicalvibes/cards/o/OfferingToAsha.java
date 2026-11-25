package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ARB", collectorNumber = "9")
public class OfferingToAsha extends Card {

    public OfferingToAsha() {
        // Counter target spell unless its controller pays {4}. You gain 4 life.
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(4));
        addSpellEffect(new GainLifeEffect(4));
    }
}
