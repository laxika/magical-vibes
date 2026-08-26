package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Removes protection from one color from a target creature until end of turn.
 */
public record RemoveProtectionFromColorUntilEndOfTurnEffect(CardColor color) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
