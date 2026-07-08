package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Creature whose attached Equipment is destroyed when combat ends (e.g. Corrosive Ooze). */
public record DestroyEquipmentAtEndOfCombat(UUID creatureId) implements DelayedAction {
}
