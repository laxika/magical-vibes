package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "38")
public class SpellContortion extends Card {

    public SpellContortion() {
        addCastingCost(RepeatableAdditionalManaCost.multikicker(List.of("{1}{U}")));
        addSpellEffect(new CounterUnlessPaysEffect(2));
        addSpellEffect(new DrawCardEffect(new RepeatedAdditionalCostCount("{1}{U}")));
    }
}
