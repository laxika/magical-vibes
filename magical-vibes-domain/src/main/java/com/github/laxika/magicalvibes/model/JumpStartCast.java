package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Jump-start: cast this spell from the graveyard by discarding a card in addition to paying its
 * other costs, then exile it.
 */
public record JumpStartCast() implements CastingOption {

    @Override
    public Disposition disposition() {
        return Disposition.EXILE;
    }

    @Override
    public List<CastingCost> costs() {
        return List.of();
    }
}
