package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller may pay {X}: they choose X (up to their available mana), that
 * mana is paid, and X copies of {@code token} are created. Models "you may pay {X}. If you do,
 * create X [tokens]" triggered abilities where the payment decision is made during resolution
 * (e.g. Rise of the Hobgoblins). Choosing X=0 means the controller declines.
 */
public record PayXManaCreateXTokensEffect(CreateTokenEffect token) implements CardEffect {
}
