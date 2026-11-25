package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSourceEquipmentCost;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "252")
public class NinjasKunai extends Card {

    public NinjasKunai() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(new SacrificeSourceEquipmentCost(), DealDamageEffect.toAnyTarget(3)),
                        "{1}, {T}, Sacrifice Ninja's Kunai: Ninja's Kunai deals 3 damage to any target."
                ),
                Subjects.attachedCreature()
        ));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
