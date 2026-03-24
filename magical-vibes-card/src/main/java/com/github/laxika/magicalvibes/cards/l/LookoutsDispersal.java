package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfControlsSubtypeEffect;

@CardRegistration(set = "XLN", collectorNumber = "62")
public class LookoutsDispersal extends Card {

    public LookoutsDispersal() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfControlsSubtypeEffect(CardSubtype.PIRATE, 1));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(4));
    }
}
