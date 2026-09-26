package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "75")
@CardRegistration(set = "MSC", collectorNumber = "392")
public class BastPantherGoddess extends Card {

    public BastPantherGoddess() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new ControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                "you control three or more creatures"));

        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new BoostTargetCreatureEffect(creaturesYouControl, creaturesYouControl));
    }
}
