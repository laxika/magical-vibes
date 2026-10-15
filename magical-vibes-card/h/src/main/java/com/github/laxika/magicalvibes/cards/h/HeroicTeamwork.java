package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "597")
public class HeroicTeamwork extends Card {

    public HeroicTeamwork() {
        addEffect(EffectSlot.SPELL, new TeamworkCost(3));
        target(TargetFilters.creature(), 1, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 1));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TeamworkCostPaid(), new DrawCardEffect(1)));
    }
}
