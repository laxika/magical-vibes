package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "STX", collectorNumber = "243")
@CardRegistration(set = "OTC", collectorNumber = "245")
public class ThrillingDiscovery extends Card {

    public ThrillingDiscovery() {
        addSpellEffect(new GainLifeEffect(2));
        addSpellEffect(new MayEffect(
                new DiscardAndDrawCardEffect(2, 3), "Discard two cards to draw three cards?"
        ));
    }
}
