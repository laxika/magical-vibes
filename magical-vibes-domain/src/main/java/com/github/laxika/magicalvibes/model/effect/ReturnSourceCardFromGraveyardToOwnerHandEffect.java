package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved as a death trigger, returns the dying creature's card from its owner's
 * graveyard to its owner's hand. Uses the stack entry's card (the source of the trigger)
 * to identify which card to return.
 *
 * <p>This is a generic self-return-on-death effect, unlike
 * {@link ReturnEnchantedCreatureToOwnerHandOnDeathEffect} which is for auras that return
 * the enchanted creature (a different card than the trigger source).
 *
 * <p>Used by Verdant Rebirth (granted via {@link GrantEffectToTargetUntilEndOfTurnEffect}).
 */
public record ReturnSourceCardFromGraveyardToOwnerHandEffect() implements CardEffect {
}
