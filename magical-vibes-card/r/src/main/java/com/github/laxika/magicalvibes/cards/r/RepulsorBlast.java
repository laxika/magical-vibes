package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSH", collectorNumber = "150")
public class RepulsorBlast extends Card {

    public RepulsorBlast() {
        addCastingCost(new TeamworkCost(2));
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(5))
                .addSpellEffect(new ConditionalEffect(
                        new TeamworkCostPaid(),
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
