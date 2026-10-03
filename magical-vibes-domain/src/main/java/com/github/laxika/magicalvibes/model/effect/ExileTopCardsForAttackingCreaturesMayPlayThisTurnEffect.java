package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Exiles as many cards as the total power of the matching creatures that attacked, then grants
 * permission to play those cards until end of turn. Matching attackers use their current power at
 * resolution; a creature that left the battlefield uses its captured last-known power.
 */
public record ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect(
        PermanentPredicate attackingCreaturePredicate,
        List<AttackingPermanentSnapshot> attackingCreatures
) implements AttackingCreaturesAwareEffect {

    public ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect(PermanentPredicate predicate) {
        this(predicate, List.of());
    }

    public ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect {
        attackingCreatures = List.copyOf(attackingCreatures);
    }

    @Override
    public CardEffect withAttackingCreatures(List<AttackingPermanentSnapshot> attackers) {
        return new ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect(
                attackingCreaturePredicate, attackers);
    }
}
