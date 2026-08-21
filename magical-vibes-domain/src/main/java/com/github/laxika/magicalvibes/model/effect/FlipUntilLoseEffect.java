package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Flips coins until the controller loses a flip, resolving the payload once for each won flip.
 */
public record FlipUntilLoseEffect(CardEffect perWin) implements CardEffect {

    public FlipUntilLoseEffect {
        Objects.requireNonNull(perWin, "perWin");
    }

    @Override
    public TargetSpec targetSpec() {
        return perWin.targetSpec();
    }
}
