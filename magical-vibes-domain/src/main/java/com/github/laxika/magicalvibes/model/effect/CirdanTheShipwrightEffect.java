package com.github.laxika.magicalvibes.model.effect;

/** Each player secretly votes for a player, then resolves Círdan's draw and hand-entry results. */
public record CirdanTheShipwrightEffect(java.util.List<java.util.UUID> noVotePlayers) implements CardEffect {
    public CirdanTheShipwrightEffect() {
        this(null);
    }

    public CirdanTheShipwrightEffect {
        noVotePlayers = noVotePlayers == null ? null : java.util.List.copyOf(noVotePlayers);
    }
}
