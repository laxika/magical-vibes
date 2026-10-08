package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission to spend mana as though it were mana of any color.
 *
 * @param allPlayers whether the permission applies to every player rather than just the controller
 */
public record SpendManaAsAnyColorEffect(boolean allPlayers) implements CardEffect {
    public SpendManaAsAnyColorEffect() {
        this(true);
    }
}
