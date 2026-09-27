package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * On resolution, the controller may pay a cost containing X: they choose X, that mana is paid,
 * and X copies of {@code token} are created. Models triggered abilities where the payment
 * decision is made during resolution (e.g. Rise of the Hobgoblins and Tilonalli's Summoner).
 * Choosing X=0 means the controller declines.
 *
 * @param manaCost the payable cost containing X, such as {@code "{X}"} or {@code "{X}{R}"}
 * @param token token blueprint; its amount is replaced with the chosen X
 * @param maximumX optional resolution-time cap for the chosen X
 */
public record PayXManaCreateXTokensEffect(String manaCost, CreateTokenEffect token,
                                           DynamicAmount maximumX) implements CardEffect {

    public PayXManaCreateXTokensEffect(CreateTokenEffect token) {
        this("{X}", token, null);
    }

    public PayXManaCreateXTokensEffect(String manaCost, CreateTokenEffect token) {
        this(manaCost, token, null);
    }

    public PayXManaCreateXTokensEffect(DynamicAmount maximumX, CreateTokenEffect token) {
        this("{X}", token, maximumX);
    }
}
