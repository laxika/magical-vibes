package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "SOS", collectorNumber = "212")
public class PrismariTheInspiration extends Card {

    public PrismariTheInspiration() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(0, 5));
        addEffect(EffectSlot.GRANT_STORM_TO_INSTANT_OR_SORCERY, new StormEffect());
    }
}
