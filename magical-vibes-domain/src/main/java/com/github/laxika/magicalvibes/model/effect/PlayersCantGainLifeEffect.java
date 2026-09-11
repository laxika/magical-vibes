package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect preventing life gain for the selected scope.
 * Life loss and damage still apply normally — only life gain is prevented.
 *
 * @param scope players affected by the restriction
 */
public record PlayersCantGainLifeEffect(Scope scope) implements CardEffect {

    public PlayersCantGainLifeEffect() {
        this(Scope.ALL_PLAYERS);
    }

    public enum Scope {
        ALL_PLAYERS,
        ENCHANTED_PLAYER
    }
}
