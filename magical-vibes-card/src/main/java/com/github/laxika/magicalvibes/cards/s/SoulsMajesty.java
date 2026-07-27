package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CON", collectorNumber = "92")
public class SoulsMajesty extends Card {

    public SoulsMajesty() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(new TargetPower()));
    }
}
