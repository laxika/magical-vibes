package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect;

@CardRegistration(set = "SOS", collectorNumber = "50")
public class FractalAnomaly extends Card {

    public FractalAnomaly() {
        // Create a 0/0 green and blue Fractal creature token and put X +1/+1 counters on it,
        // where X is the number of cards you've drawn this turn.
        addEffect(EffectSlot.SPELL, new CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect());
    }
}
