package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Static effect: creatures that do NOT match the exemption predicate can't attack (CR 508.1c).
 * This is a global restriction checked against all permanents on the battlefield, the exemption judged
 * relative to the source (its controller and card).
 * Example: Stormtide Leviathan — "Creatures without flying or islandwalk can't attack."
 * The exemptionPredicate would match creatures WITH flying or islandwalk.
 * {@link #youControl} restricts only the source controller's creatures: Evil Eye of Orms-by-Gore — "Non-Eye
 * creatures you control can't attack."
 *
 * @param exemptionPredicate creatures matching this predicate ARE allowed to attack
 */
public record CreaturesCantAttackUnlessPredicateEffect(PermanentPredicate exemptionPredicate) implements CardEffect {

    /**
     * "[Creatures] you control can't attack" except those matching {@code exemption}: a creature another player
     * controls is exempt too.
     */
    public static CreaturesCantAttackUnlessPredicateEffect youControl(PermanentPredicate exemption) {
        return new CreaturesCantAttackUnlessPredicateEffect(new PermanentAnyOfPredicate(List.of(
                exemption, new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
    }
}
