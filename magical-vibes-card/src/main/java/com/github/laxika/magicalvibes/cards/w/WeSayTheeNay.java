package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;

@CardRegistration(set = "MSH", collectorNumber = "82")
public class WeSayTheeNay extends Card {

    public WeSayTheeNay() {
        addEffect(EffectSlot.SPELL, new TeamworkCost(2));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new TeamworkCostPaid(),
                new CounterUnlessPaysEffect(2),
                new CounterUnlessPaysEffect(4)
        ));
    }
}
