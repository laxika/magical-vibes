package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect for creatures dealt damage this turn by a source controlled by the
 * effect's controller. Such creatures are exiled instead of being put into a graveyard when
 * they would die.
 *
 * <p>This is a static effect. {@code PermanentRemovalService} evaluates the source-control and
 * damage history conditions when the creature would move to a graveyard.
 */
public record ExileCreaturesDamagedByControlledSourceInsteadOfDyingEffect() implements CardEffect {
}
