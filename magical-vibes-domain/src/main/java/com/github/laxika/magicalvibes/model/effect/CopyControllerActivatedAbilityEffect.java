package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Resolution-time effect that creates a single copy of an activated ability for its controller
 * (CR 707.10). The copy is put on the stack above the original; the copy's controller may choose
 * new targets for it.
 *
 * <p>Populated at trigger time by {@code TriggerCollectionService} from
 * {@link CopyControllerActivatedAbilityTriggerEffect}. The snapshot preserves the ability's state
 * (effects, targets, X value, source) at activation time. The {@code ability} reference is retained
 * so the "you may choose new targets" prompt can recompute the legal targets for the copy.</p>
 *
 * <p>Used by Rings of Brighthearth.</p>
 *
 * @param abilitySnapshot   snapshot of the activated ability on the stack at trigger time
 * @param ability           the activated ability that was activated (for retargeting the copy)
 * @param activatingPlayerId the player who activated the ability (and controls the copy)
 */
public record CopyControllerActivatedAbilityEffect(
        StackEntry abilitySnapshot,
        ActivatedAbility ability,
        UUID activatingPlayerId
) implements CardEffect {
}
