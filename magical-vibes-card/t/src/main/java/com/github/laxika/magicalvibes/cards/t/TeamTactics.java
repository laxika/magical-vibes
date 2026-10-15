package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSH", collectorNumber = "155")
public class TeamTactics extends Card {

    public TeamTactics() {
        addEffect(EffectSlot.SPELL, new TeamworkCost(1));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TeamworkCostPaid(),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));
    }
}
