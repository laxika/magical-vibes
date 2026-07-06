package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/**
 * Standard equip activated ability: sorcery-speed, targets a creature you control.
 * Use this for any equipment with a mana-cost-only equip ability.
 */
public class EquipActivatedAbility extends ActivatedAbility {

    public EquipActivatedAbility(String manaCost) {
        super(
                false,
                manaCost,
                List.of(new EquipEffect()),
                "Equip " + manaCost,
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        );
    }
}
