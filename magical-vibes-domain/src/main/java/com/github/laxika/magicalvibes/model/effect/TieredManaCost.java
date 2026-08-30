package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Mode-dependent additional mana cost for a tiered modal spell.
 *
 * @param additionalManaCosts additional mana costs in the modal spell's mode order
 */
public record TieredManaCost(List<String> additionalManaCosts) implements CostEffect {

    public TieredManaCost {
        if (additionalManaCosts == null || additionalManaCosts.isEmpty()
                || additionalManaCosts.stream().anyMatch(cost -> cost == null)) {
            throw new IllegalArgumentException("Tiered mana costs must contain at least one non-null cost");
        }
        additionalManaCosts = List.copyOf(additionalManaCosts);
    }
}
