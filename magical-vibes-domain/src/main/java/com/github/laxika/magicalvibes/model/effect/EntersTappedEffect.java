package com.github.laxika.magicalvibes.model.effect;

/**
 * "This permanent enters the battlefield tapped."
 * Replacement effect — applied during entry, never goes on the stack.
 * Added as a static ability.
 */
public record EntersTappedEffect() implements ReplacementEffect {
}
