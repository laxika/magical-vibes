package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "17")
public class GauntletsOfLight extends Card {

    public GauntletsOfLight() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(0, 2, Subjects.attachedCreature()))
                .addStaticEffect(new AssignCombatDamageWithToughnessEffect(Subjects.attachedCreature(), true))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{2}{W}",
                                List.of(new UntapPermanentsEffect(Subjects.source())),
                                "{2}{W}: Untap this creature."
                        ),
                        Subjects.attachedCreature()
                ));
    }
}
