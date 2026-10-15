package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GhyrsonStarnKelermorphEffect;

@CardRegistration(set = "40K", collectorNumber = "124")
public class GhyrsonStarnKelermorph extends Card {

    public GhyrsonStarnKelermorph() {
        // Ward {2}
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
        addEffect(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, new GhyrsonStarnKelermorphEffect());
    }
}
