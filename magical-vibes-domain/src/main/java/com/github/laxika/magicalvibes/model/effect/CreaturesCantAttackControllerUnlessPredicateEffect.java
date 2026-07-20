package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: creatures that do NOT match the exemption predicate can't attack this
 * permanent's controller (they may still attack other players). Unlike
 * {@link CreaturesCantAttackUnlessPredicateEffect}, this is a defender-scoped restriction
 * evaluated per attacked player at attack-declaration time.
 * Example: Form of the Dragon — "Creatures without flying can't attack you." The
 * exemptionPredicate would match creatures WITH flying.
 * When {@code protectsPlaneswalkers} is true the restriction also covers attacks aimed at the
 * controller's planeswalkers (Sandwurm Convergence — "can't attack you or planeswalkers you
 * control").
 *
 * @param exemptionPredicate    creatures matching this predicate ARE allowed to attack the controller
 * @param protectsPlaneswalkers whether the restriction also forbids attacking the controller's planeswalkers
 */
public record CreaturesCantAttackControllerUnlessPredicateEffect(
        PermanentPredicate exemptionPredicate, boolean protectsPlaneswalkers) implements CardEffect {

    public CreaturesCantAttackControllerUnlessPredicateEffect(PermanentPredicate exemptionPredicate) {
        this(exemptionPredicate, false);
    }
}
