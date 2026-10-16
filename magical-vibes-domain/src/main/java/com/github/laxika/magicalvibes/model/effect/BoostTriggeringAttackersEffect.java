package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * An attack-group effect for "whenever you attack with one or more [matching] creatures, those
 * creatures get +P/+T until end of turn". The matching attackers are bound when the trigger is
 * created; creatures that have left the battlefield by resolution are skipped. The trigger is not
 * created when no attacker matches.
 */
public record BoostTriggeringAttackersEffect(
        PermanentPredicate attackingCreaturePredicate,
        int powerBoost,
        int toughnessBoost,
        List<AttackingPermanentSnapshot> attackingCreatures
) implements AttackingCreaturesAwareEffect {

    public BoostTriggeringAttackersEffect(PermanentPredicate predicate, int powerBoost, int toughnessBoost) {
        this(predicate, powerBoost, toughnessBoost, List.of());
    }

    public BoostTriggeringAttackersEffect {
        attackingCreatures = List.copyOf(attackingCreatures);
    }

    @Override
    public CardEffect withAttackingCreatures(List<AttackingPermanentSnapshot> attackers) {
        return new BoostTriggeringAttackersEffect(attackingCreaturePredicate, powerBoost, toughnessBoost, attackers);
    }
}
