package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/** Creates a copy of a triggered ability captured when its trigger was created. */
public record CopyTriggeredAbilityFromSnapshotEffect(StackEntry abilitySnapshot) implements CardEffect {
}
