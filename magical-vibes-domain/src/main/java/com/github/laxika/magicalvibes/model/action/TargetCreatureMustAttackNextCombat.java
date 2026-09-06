package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Marks a creature to attack during its controller's next combat phase if able.
 */
public record TargetCreatureMustAttackNextCombat(UUID permanentId) implements DelayedAction {
}
