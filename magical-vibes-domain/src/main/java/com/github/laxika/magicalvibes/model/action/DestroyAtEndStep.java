package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Permanent scheduled for destruction at the beginning of the next end step (e.g. Stone Giant). */
public record DestroyAtEndStep(UUID permanentId) implements DelayedAction {
}
