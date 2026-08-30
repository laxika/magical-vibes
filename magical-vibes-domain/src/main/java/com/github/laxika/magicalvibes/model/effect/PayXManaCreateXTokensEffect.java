package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller may pay a cost containing X: they choose X, that mana is paid,
 * and X copies of {@code token} are created. Models triggered abilities where the payment
 * decision is made during resolution (e.g. Rise of the Hobgoblins and Tilonalli's Summoner).
 * Choosing X=0 means the controller declines.
 *
 * @param manaCost the payable cost containing X, such as {@code "{X}"} or {@code "{X}{R}"}
 */
public record PayXManaCreateXTokensEffect(String manaCost, CreateTokenEffect token) implements CardEffect {

    public PayXManaCreateXTokensEffect(CreateTokenEffect token) {
        this("{X}", token);
    }
}
