package com.github.laxika.magicalvibes.model.effect;

/**
 * As-enters replacement effect (CR 614.1c): "This creature enters with a token copy of [Aura] attached to
 * it." The token enters at the same time as the permanent (no ETB trigger, no stack use), already attached
 * to it. Token-creation replacement effects (Doubling Season, ...) apply to it.
 *
 * @param token    blueprint of the Aura token (an Enchantment with the Aura subtype and its static effects)
 * @param manaCost printed mana cost of the copied Aura, kept by the token because a copy has the copiable
 *                 values of the original (CR 707.2)
 */
public record EnterWithTokenAttachedEffect(CreateTokenEffect token, String manaCost) implements ReplacementEffect {
}
