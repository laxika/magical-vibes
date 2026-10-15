package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfDefendingPlayerCreatureAndAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "179")
@CardRegistration(set = "WHO", collectorNumber = "784")
public class MidnightCrusaderShuttle extends Card {

    public MidnightCrusaderShuttle() {
        addEffect(EffectSlot.ON_ATTACK, ForcedCostOrElseEffect.defendingPlayerMayPay(
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice a creature"),
                List.of(new GainControlOfDefendingPlayerCreatureAndAttackEffect())));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
