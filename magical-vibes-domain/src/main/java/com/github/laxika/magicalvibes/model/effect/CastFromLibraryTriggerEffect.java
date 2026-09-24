package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Trigger descriptor for "whenever you cast a creature spell from your library" abilities. */
public record CastFromLibraryTriggerEffect(
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
