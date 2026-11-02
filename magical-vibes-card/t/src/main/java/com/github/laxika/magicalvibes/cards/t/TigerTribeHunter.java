package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "163")
public class TigerTribeHunter extends Card {

    public TigerTribeHunter() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(6),
                new MayEffect(
                        new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(
                                new PermanentIsCreaturePredicate()),
                        "Sacrifice another creature?")));
    }
}
