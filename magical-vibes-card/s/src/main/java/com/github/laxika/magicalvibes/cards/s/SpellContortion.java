package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "38")
public class SpellContortion extends Card {

    public SpellContortion() {
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.multikicker(List.of("{1}{U}")));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new RepeatedAdditionalCostCount("{1}{U}")));
    }
}
