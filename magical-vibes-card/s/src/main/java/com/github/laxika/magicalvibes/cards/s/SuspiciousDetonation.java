package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedArtifactThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "145")
public class SuspiciousDetonation extends Card {

    public SuspiciousDetonation() {
        addStaticEffect(new ConditionalEffect(
                new ControllerSacrificedArtifactThisTurn(), new ReduceOwnCastCostEffect(new Fixed(3))));
        addStaticEffect(new CantBeCounteredEffect());
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(4));
    }
}
