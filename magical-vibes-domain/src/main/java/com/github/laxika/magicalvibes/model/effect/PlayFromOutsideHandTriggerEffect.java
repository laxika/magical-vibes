package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Trigger descriptor for casting a spell or playing a land from a zone other than hand. */
public record PlayFromOutsideHandTriggerEffect(List<CardEffect> resolvedEffects) implements CardEffect {
}
