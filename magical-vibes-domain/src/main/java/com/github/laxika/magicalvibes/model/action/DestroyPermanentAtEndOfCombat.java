package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Permanent scheduled for destruction when combat ends (e.g. a Basilisk-style "destroy that
 * creature at end of combat" trigger). {@code cannotBeRegenerated} controls whether regeneration
 * shields are ignored.
 */
public record DestroyPermanentAtEndOfCombat(UUID permanentId, boolean cannotBeRegenerated) implements DelayedAction {
}
