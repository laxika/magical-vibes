package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** At the beginning of the next end step, destroy the given permanent if it attacked this turn. */
public record DestroyPermanentIfAttackedAtEndStep(UUID permanentId) implements DelayedAction {
}
