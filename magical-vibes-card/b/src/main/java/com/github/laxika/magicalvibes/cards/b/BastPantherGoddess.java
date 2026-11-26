package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "75")
@CardRegistration(set = "MSC", collectorNumber = "392")
public class BastPantherGoddess extends Card {

    public BastPantherGoddess() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attackOrBlock(
                new ControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                "you control three or more creatures"));

        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        target(TargetFilters.attackingCreature()).addTrigger(Triggers.youAttack(),
                BoostEffect.target(creaturesYouControl, creaturesYouControl));
    }
}
