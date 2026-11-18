package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: the combat opponent (the creature this permanent blocks) can't attack during its
 * controller's next turn. Wall of Dust's "Whenever this creature blocks a creature, that creature
 * can't attack during its controller's next turn."
 * <p>
 * Placed on the {@code Triggers.blocks()} ability, auto-targeting
 * the blocked attacker. The referenced creature is passed as the stack entry's target but the trigger
 * does not target (it can't fizzle). At resolution this effect goes on that creature as a floating effect
 * {@code Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN}: it begins as its controller's next turn begins and ends with
 * that turn.
 */
public record CantAttackNextTurnCombatOpponentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
