package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

public class BallistaWielder extends Card {

    public BallistaWielder() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(DealDamageEffect.toAnyTarget(1),
                        new CantBlockThisTurnEffect(
                                Subjects.thisWay(StepKind.DEAL_DAMAGE, new PermanentIsCreaturePredicate()))),
                "{2}{R}: This creature deals 1 damage to any target. A creature dealt damage this way can't block this turn."
        ));
    }
}
