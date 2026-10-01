package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;
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
 * the battlefield tapped under its owner's control.'") and Undying Malice (which also returns it
 * with a +1/+1 counter). Fizzles if the card is no longer in a graveyard.
 *
 * @param tapped              {@code true} to have it enter the battlefield tapped
 * @param losesAllAbilities   {@code true} to make the returned permanent lose all abilities indefinitely
 * @param overriddenCardTypes non-empty to replace the returned permanent's card types indefinitely
 * @param perpetuallyLosesThisAbility {@code true} to make this triggered ability stop triggering
 *                                    for this card identity after resolution
 */
public record ReturnSourceCardFromGraveyardToBattlefieldEffect(
        boolean tapped,
        boolean losesAllAbilities,
        Set<CardType> overriddenCardTypes, CounterType enterWithCounter,
        boolean perpetuallyLosesThisAbility)
        implements CardEffect {

    public ReturnSourceCardFromGraveyardToBattlefieldEffect {
        overriddenCardTypes = Set.copyOf(overriddenCardTypes);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, boolean losesAllAbilities,
                                                             Set<CardType> overriddenCardTypes,
                                                             CounterType enterWithCounter) {
        this(tapped, losesAllAbilities, overriddenCardTypes, enterWithCounter, false);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped) {
        this(tapped, false, Set.of(), null, false);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, boolean losesAllAbilities) {
        this(tapped, losesAllAbilities, Set.of(), null, false);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, boolean losesAllAbilities,
                                                             boolean perpetuallyLosesThisAbility) {
        this(tapped, losesAllAbilities, Set.of(), null, perpetuallyLosesThisAbility);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped,
                                                             Set<CardType> overriddenCardTypes) {
        this(tapped, false, overriddenCardTypes, null, false);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, CounterType enterWithCounter) {
        this(tapped, false, Set.of(), enterWithCounter, false);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, boolean losesAllAbilities, CounterType enterWithCounter) {
        this(tapped, losesAllAbilities, Set.of(), enterWithCounter, false);
    }
}
