package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Resolves a list of static player effects for the controller until end of turn.
 * The nested effects are stored as markers and are not resolved independently.
 */
public record GrantPlayerStaticEffectsUntilEndOfTurnEffect(List<CardEffect> effects) implements CardEffect {

    public GrantPlayerStaticEffectsUntilEndOfTurnEffect {
        effects = List.copyOf(effects);
    }
}
