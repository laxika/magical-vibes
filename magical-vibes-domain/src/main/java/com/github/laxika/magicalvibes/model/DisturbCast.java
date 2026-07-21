package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Disturb (CR 702.146): cast this double-faced card transformed from the graveyard for its
 * disturb cost. The resulting permanent enters with its back face up. Exile-if-to-graveyard
 * is printed on the back face as {@code ExileInsteadOfGraveyardReplacementEffect}.
 */
public record DisturbCast(List<CastingCost> costs) implements CastingOption {

    public DisturbCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        // Back-face exile replacement handles disposition; casting option itself does not force exile.
        return Disposition.GRAVEYARD;
    }
}
