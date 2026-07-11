package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Token permanent scheduled for exile when combat ends (e.g. Geist of Saint Traft's Angel). */
public record ExileTokenAtEndOfCombat(UUID permanentId) implements DelayedAction {
}
