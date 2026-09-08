package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.ActivationCostModifierEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Standard equip activated ability: sorcery-speed, targets a creature you control.
 * Use this for any equipment with a mana-cost-only equip ability.
 */
public class EquipActivatedAbility extends ActivatedAbility {

    public EquipActivatedAbility(String manaCost) {
        this(manaCost, null, null, null);
    }

    /**
     * Standard equip ability with an activation-cost modifier that is evaluated using the chosen
     * creature target.
     */
    public EquipActivatedAbility(String manaCost, ActivationCostModifierEffect activationCostModifier) {
        this(manaCost, null, null, activationCostModifier);
    }

    /**
     * Equip restricted to a subset of the creatures you control ("can be attached only to a
     * legendary creature" — Konda's Banner). Pair with {@code Card.setAttachRestriction} so the
     * same requirement is also enforced continuously as a state-based action (CR 704.5n).
     */
    public EquipActivatedAbility(String manaCost, PermanentPredicate restriction, String failureMessage) {
        this(manaCost, restriction, failureMessage, null);
    }

    private EquipActivatedAbility(String manaCost, PermanentPredicate restriction, String failureMessage,
                                  ActivationCostModifierEffect activationCostModifier) {
        super(
                false,
                manaCost,
                activationCostModifier == null
                        ? List.of(new EquipEffect())
                        : List.of(activationCostModifier, new EquipEffect()),
                "Equip " + manaCost,
                new ControlledPermanentPredicateTargetFilter(
                        restriction == null
                                ? new PermanentIsCreaturePredicate()
                                : new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), restriction)),
                        failureMessage == null ? "Target must be a creature you control" : failureMessage
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        );
    }
}
