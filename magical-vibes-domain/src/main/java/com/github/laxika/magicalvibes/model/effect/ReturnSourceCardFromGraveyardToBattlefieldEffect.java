package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger: immediately returns the dying source card from its owner's graveyard to the
 * battlefield under that owner's control, optionally tapped and with an optional counter.
 *
 * <p>Battlefield analogue of {@link ReturnSourceCardFromGraveyardToOwnerHandEffect}. Unlike
 * {@link RegisterDelayedSelfReturnFromGraveyardEffect} the return happens right away instead of
 * being queued for a later step, and is granted by a card rather than pushed by the engine.
 *
 * <p>Granted until end of turn by Abnormal Endurance ("gains 'When this creature dies, return it to
 * the battlefield tapped under its owner's control.'"). Fizzles if the card is no longer in a
 * graveyard.
 *
 * @param tapped             {@code true} to have it enter the battlefield tapped
 * @param losesAllAbilities  {@code true} to make the returned permanent lose all abilities indefinitely
 * @param enterWithCounter   optional counter put on the returned permanent as it enters
 */
public record ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped,
                                                               boolean losesAllAbilities,
                                                               CounterType enterWithCounter)
        implements CardEffect {

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped) {
        this(tapped, false, null);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, CounterType enterWithCounter) {
        this(tapped, false, enterWithCounter);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, boolean losesAllAbilities) {
        this(tapped, losesAllAbilities, null);
    }
}
