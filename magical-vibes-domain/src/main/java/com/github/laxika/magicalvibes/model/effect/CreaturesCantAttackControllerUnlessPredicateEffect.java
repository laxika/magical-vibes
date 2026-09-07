package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

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
 * When {@code restrictedAttackerId} is non-null, only creatures controlled by that player are
 * restricted; this is used by temporary effects that apply to one opponent.
 *
 * @param exemptionPredicate    creatures matching this predicate ARE allowed to attack the controller
 * @param protectsPlaneswalkers whether the restriction also forbids attacking the controller's planeswalkers
 * @param restrictedAttackerId  optional attacking player to restrict, or {@code null} for all attackers
 */
public record CreaturesCantAttackControllerUnlessPredicateEffect(
        PermanentPredicate exemptionPredicate, boolean protectsPlaneswalkers,
        UUID restrictedAttackerId) implements CardEffect {

    public CreaturesCantAttackControllerUnlessPredicateEffect(PermanentPredicate exemptionPredicate) {
        this(exemptionPredicate, false, null);
    }

    public CreaturesCantAttackControllerUnlessPredicateEffect(PermanentPredicate exemptionPredicate,
                                                               boolean protectsPlaneswalkers) {
        this(exemptionPredicate, protectsPlaneswalkers, null);
    }
}
