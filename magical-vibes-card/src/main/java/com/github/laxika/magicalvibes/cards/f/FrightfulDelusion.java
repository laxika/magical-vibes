package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "57")
public class FrightfulDelusion extends Card {

    public FrightfulDelusion() {
        // Discard placed before counter so the target spell is still on the stack
        // and we can identify "that player" (the target spell's controller).
        addEffect(EffectSlot.SPELL, new TargetSpellControllerDiscardsEffect(1));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));
    }
}
