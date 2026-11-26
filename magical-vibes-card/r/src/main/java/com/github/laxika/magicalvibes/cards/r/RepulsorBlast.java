package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSH", collectorNumber = "150")
public class RepulsorBlast extends Card {

    public RepulsorBlast() {
        addCastingCost(new TeamworkCost(2));
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(5))
                .addSpellEffect(new ConditionalEffect(
                        new TeamworkCostPaid(),
                        DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2)));
    }
}
