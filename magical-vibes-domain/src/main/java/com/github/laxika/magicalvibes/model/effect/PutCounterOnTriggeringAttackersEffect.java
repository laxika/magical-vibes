package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * An attack-group effect for "whenever you attack with one or more [matching] creatures, put a
 * [counter] on each of them". The matching attackers are bound when the trigger is created, so
 * "each of them" stays fixed even if a creature leaves combat or a later creature enters attacking
 * before resolution. The trigger is not created when no attacker matches.
 */
public record PutCounterOnTriggeringAttackersEffect(
        CounterType counterType,
        PermanentPredicate attackingCreaturePredicate,
        List<AttackingPermanentSnapshot> attackingCreatures
) implements AttackingCreaturesAwareEffect {

    public PutCounterOnTriggeringAttackersEffect(CounterType counterType, PermanentPredicate predicate) {
        this(counterType, predicate, List.of());
    }

    public PutCounterOnTriggeringAttackersEffect {
        attackingCreatures = List.copyOf(attackingCreatures);
    }

    @Override
    public CardEffect withAttackingCreatures(List<AttackingPermanentSnapshot> attackers) {
        return new PutCounterOnTriggeringAttackersEffect(counterType, attackingCreaturePredicate, attackers);
    }
}
