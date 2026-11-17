package com.github.laxika.magicalvibes.model.effect;

/**
 * "Up to one target creature an opponent controls attacks this permanent during its controller's
 * next turn if able." (Gideon, Battle-Forged's +2).
 *
 * <p>Targets a single creature. The single-creature sibling of
 * {@link MustAttackNextTurnEffect} with {@link TauntTarget#SOURCE_PERMANENT}, which taunts every
 * creature a targeted player controls: this one makes only the targeted creature
 * attack the ability's source permanent, with a floating {@link CreaturesMustAttackTargetPlayerUntilNextTurnEffect}
 * on the creature that begins when its controller's next turn begins and ends at that turn's cleanup step. The requirement lapses on its own if the source permanent is no longer
 * a legal attack target, and per CR 508.1d the affected player is not required to pay attack costs.
 */
public record TargetCreatureMustAttackSourcePermanentNextTurnEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
