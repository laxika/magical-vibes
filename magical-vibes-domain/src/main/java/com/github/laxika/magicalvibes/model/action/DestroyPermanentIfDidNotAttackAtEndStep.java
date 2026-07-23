package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * At the beginning of the next end step, destroy the given permanent if it did not attack this
 * turn. Used by Norritt (single-target analogue of {@link DestroyNonAttackersAtEndStep}).
 */
public record DestroyPermanentIfDidNotAttackAtEndStep(UUID permanentId) implements DelayedAction {
}
