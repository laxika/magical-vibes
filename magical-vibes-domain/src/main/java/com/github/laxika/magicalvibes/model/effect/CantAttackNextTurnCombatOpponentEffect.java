package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: the combat opponent (the creature this permanent blocks) can't attack during its
 * controller's next turn. Wall of Dust's "Whenever this creature blocks a creature, that creature
 * can't attack during its controller's next turn."
 * <p>
 * Placed on the {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BLOCK} slot, auto-targeting
 * the blocked attacker. The referenced creature is passed as the stack entry's target but the trigger
 * does not target (it can't fizzle). At resolution the opponent's {@code cantAttackNextTurn} flag is
 * set; the turn engine promotes it to an active restriction at the start of that creature's
 * controller's next turn, so it lasts exactly that one turn.
 */
public record CantAttackNextTurnCombatOpponentEffect() implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
