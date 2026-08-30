package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect;

@CardRegistration(set = "THB", collectorNumber = "81")
public class WhirlwindDenial extends Card {

    public WhirlwindDenial() {
        addEffect(EffectSlot.SPELL, new CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect(4));
    }
}
