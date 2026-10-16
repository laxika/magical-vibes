package com.github.laxika.magicalvibes.model.effect;

/**
 * Hullbreaker Horror's modal spell-cast trigger: "Whenever you cast a spell, choose up to one —
 * return target spell you don't control to its owner's hand; or return target nonland permanent to
 * its owner's hand."
 * <p>
 * Placed via {@link SpellCastTriggerEffect} in {@code ON_CONTROLLER_CASTS_SPELL}. The engine has no
 * cast-time modal machinery for triggered abilities, so the mode (+ target) are announced by
 * {@code StackedModalTriggerService} as the ability goes on the stack (CR 603.3c). A "do nothing"
 * mode covers "up to one": choosing it removes the ability from the stack.
 */
public record HullbreakerHorrorTriggerEffect() implements CardEffect {
}
