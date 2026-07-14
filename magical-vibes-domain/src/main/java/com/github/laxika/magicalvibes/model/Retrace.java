package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Retrace (CR 702.81): "You may cast this card from your graveyard by discarding a land card in
 * addition to paying its other costs." Uses the card's normal mana cost plus the additional cost of
 * discarding a land card, and — unlike flashback — does not exile the card afterwards, so it goes to
 * the graveyard normally and can be retraced again (e.g. Cenn's Enlistment).
 */
public record Retrace() implements CastingOption {

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        return List.of();
    }
}
