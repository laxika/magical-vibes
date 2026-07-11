package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Permanent scheduled to be exiled and returned transformed when combat ends (e.g. Conqueror's Galleon). */
public record ExileAndReturnTransformedAtEndOfCombat(UUID permanentId) implements DelayedAction {
}
