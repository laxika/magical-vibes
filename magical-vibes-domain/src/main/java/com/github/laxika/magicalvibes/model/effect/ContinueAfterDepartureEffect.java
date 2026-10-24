package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Internal resolution step: the rest of {@code departingEffect} after the departure it ended its own step on
 * (a CR 616.1 question was asked before the permanent left). The engine inserts it right after that step, so it
 * runs once the departure is done and its results are on the resolution scratchpad. Never put on a card.
 *
 * @param departingEffect the effect whose handler departed a permanent and carries on here
 */
public record ContinueAfterDepartureEffect(CardEffect departingEffect) implements CardEffect {

    public ContinueAfterDepartureEffect {
        Objects.requireNonNull(departingEffect, "departingEffect");
    }
}
