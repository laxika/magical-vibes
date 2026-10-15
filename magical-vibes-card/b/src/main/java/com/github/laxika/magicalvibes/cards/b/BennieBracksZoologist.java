package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCreatedTokenThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "NCC", collectorNumber = "86")
@CardRegistration(set = "NCC", collectorNumber = "94")
@CardRegistration(set = "MKC", collectorNumber = "57")
public class BennieBracksZoologist extends Card {

    public BennieBracksZoologist() {
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(new ControllerCreatedTokenThisTurn(), new DrawCardEffect()));
    }
}
