package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

/**
 * Reduce // Rubble — front half (Reduce).
 * Instant — Counter target spell unless its controller pays {3}.
 * Back half (Rubble) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "216")
@CardRegistration(set = "AKR", collectorNumber = "254")
public class ReduceRubble extends Card {

    public ReduceRubble() {
        setBackFaceCard(new Rubble());

        // Counter target spell unless its controller pays {3}.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));
    }

    @Override
    public String getBackFaceClassName() {
        return "Rubble";
    }
}
