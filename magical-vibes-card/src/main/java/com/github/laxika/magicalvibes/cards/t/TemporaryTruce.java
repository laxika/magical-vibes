package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawUpToNGainLifePerCardBelowEffect;

@CardRegistration(set = "POR", collectorNumber = "33")
public class TemporaryTruce extends Card {

    public TemporaryTruce() {
        // Each player may draw up to two cards; for each card less than two drawn, that player gains 2 life.
        addEffect(EffectSlot.SPELL, new EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(2, 2));
    }
}
