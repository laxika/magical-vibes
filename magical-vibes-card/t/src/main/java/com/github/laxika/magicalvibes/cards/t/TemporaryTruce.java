package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawUpToNGainLifePerCardBelowEffect;

@CardRegistration(set = "POR", collectorNumber = "33")
@CardRegistration(set = "MB2", collectorNumber = "22")
public class TemporaryTruce extends Card {

    public TemporaryTruce() {
        // Each player may draw up to two cards; for each card less than two drawn, that player gains 2 life.
        addSpellEffect(new EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(2, 2));
    }
}
