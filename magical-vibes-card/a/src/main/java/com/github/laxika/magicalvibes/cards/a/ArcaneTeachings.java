package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "188")
@CardRegistration(set = "JUD", collectorNumber = "78")
public class ArcaneTeachings extends Card {

    public ArcaneTeachings() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(DealDamageEffect.toAnyTarget(1)),
                                "{T}: This creature deals 1 damage to any target."
                        ),
                        Subjects.attachedCreature()
                ));
    }
}
