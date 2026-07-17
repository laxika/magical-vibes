package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Nontoken permanent scheduled for exile at the beginning of the next end step (e.g. Dark Maze). */
public record ExilePermanentAtEndStep(UUID permanentId) implements DelayedAction {
}
