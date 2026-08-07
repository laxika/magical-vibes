package com.github.laxika.magicalvibes.model.effect;

/**
 * "Up to one target creature an opponent controls attacks this permanent during its controller's
 * next turn if able." (Gideon, Battle-Forged's +2).
 *
 * <p>Targets a single creature. The single-creature sibling of
 * {@link MustAttackNextTurnEffect} with {@link TauntTarget#SOURCE_PERMANENT}, which taunts every
 * creature a targeted player controls: this one records only the targeted creature in
 * {@code GameData.creatureMustAttackPermanentNextTurn}, keyed by that creature's permanent id and
 * mapped to the ability's source permanent. When the creature's controller's next turn begins the
 * turn engine promotes the entry onto the creature itself ({@code mustAttackThisTurn} plus
 * {@code mustAttackTargetId}), the same transient pair Alluring Siren sets, so declare-attackers
 * enforcement is unchanged. The requirement lapses on its own if the source permanent is no longer
 * a legal attack target, and per CR 508.1d the affected player is not required to pay attack costs.
 */
public record TargetCreatureMustAttackSourcePermanentNextTurnEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
