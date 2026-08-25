package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Trigger descriptor for "whenever you play a land from exile" abilities. */
public record LandPlayFromExileTriggerEffect(List<CardEffect> resolvedEffects) implements CardEffect {
}
