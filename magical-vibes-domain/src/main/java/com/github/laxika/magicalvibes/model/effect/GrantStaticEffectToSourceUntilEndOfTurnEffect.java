package com.github.laxika.magicalvibes.model.effect;

/** Grants the source permanent a static effect until end of turn. */
public record GrantStaticEffectToSourceUntilEndOfTurnEffect(CardEffect staticEffect)
        implements CardEffect {
}
