package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PutCounterCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "131")
public class CinderStrike extends Card {

    public CinderStrike() {
        addCastingCost(new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1, true));

        SpellTarget creatureTarget = target(TargetFilters.creature());
        creatureTarget.addSpellEffect(new ConditionalEffect(new PutCounterCostPaid(), new DealDamageToTargetCreatureEffect(4)));
        creatureTarget.addSpellEffect(new ConditionalEffect(new NotCondition(new PutCounterCostPaid()),
                        new DealDamageToTargetCreatureEffect(2)));
    }
}
