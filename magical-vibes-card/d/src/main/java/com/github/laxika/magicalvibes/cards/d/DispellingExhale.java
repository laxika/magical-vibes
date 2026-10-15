package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BeholdCostPaid;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "TDM", collectorNumber = "41")
public class DispellingExhale extends Card {

    public DispellingExhale() {
        addEffect(EffectSlot.SPELL, BeholdCost.optional(CardSubtype.DRAGON));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new BeholdCostPaid(), new CounterUnlessPaysEffect(2), new CounterUnlessPaysEffect(4)));
    }
}
