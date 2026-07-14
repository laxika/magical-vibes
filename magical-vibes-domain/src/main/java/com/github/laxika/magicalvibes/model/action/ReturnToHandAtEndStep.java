package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Permanent scheduled to be returned to its owner's hand at the beginning of the next end step (e.g. Dragon Mask). */
public record ReturnToHandAtEndStep(UUID permanentId) implements DelayedAction {
}
