package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Token permanent scheduled for exile at the beginning of the next end step (e.g. Mimic Vat). */
public record ExileTokenAtEndStep(UUID permanentId) implements DelayedAction {
}
