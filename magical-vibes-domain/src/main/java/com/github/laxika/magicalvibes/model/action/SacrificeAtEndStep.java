package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Permanent scheduled for sacrifice at the beginning of the next end step (e.g. Choreographed Sparks). */
public record SacrificeAtEndStep(UUID permanentId) implements DelayedAction {
}
