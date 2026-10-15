package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;

@CardRegistration(set = "MSC", collectorNumber = "641")
public class TimelineInquiry extends Card {

    public TimelineInquiry() {
        addEffect(EffectSlot.SPELL, new TeamworkCost(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new TeamworkCostPaid()),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
    }
}
