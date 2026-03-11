package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, the targeted creature must attack this turn if able.
 * Sets a transient flag on the Permanent that is cleared at end of turn
 * via {@code resetModifiers()}.
 * Per CR 508.1d, the controller is not required to pay any attack costs
 * (e.g. Ghostly Prison tax) even if this effect is present.
 *
 * @param forceAttackController if {@code true}, the creature must specifically attack the
 *                              controller of this effect (e.g. Alluring Siren: "attacks you
 *                              this turn if able"). If {@code false}, the creature must attack
 *                              but may choose any legal target (e.g. Incite: "attacks this turn
 *                              if able").
 */
public record MustAttackThisTurnEffect(boolean forceAttackController) implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
