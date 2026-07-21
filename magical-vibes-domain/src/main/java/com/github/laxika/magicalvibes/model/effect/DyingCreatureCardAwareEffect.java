package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Capability marker for {@code ON_DAMAGED_CREATURE_DIES} effects that must act on the specific
 * creature card that died. The trigger collector binds the dying creature's card id at collection
 * time, because the "you may …" resolution flow ({@link MayEffect} / {@code PendingMayAbility})
 * does not carry the stack entry's triggering-card id through to the wrapped effect.
 */
public interface DyingCreatureCardAwareEffect {

    /** Returns a copy of this effect with the dying creature's card id bound in. */
    CardEffect boundToDyingCard(UUID dyingCardId);
}
