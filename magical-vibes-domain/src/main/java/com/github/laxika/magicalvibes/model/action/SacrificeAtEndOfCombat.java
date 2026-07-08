package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Permanent scheduled for sacrifice when combat ends (e.g. an attack-triggered temporary creature). */
public record SacrificeAtEndOfCombat(UUID permanentId) implements DelayedAction {
}
