package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Death trigger: immediately returns the dying source card from its owner's graveyard to the
 * battlefield under that owner's control, optionally tapped.
 *
 * <p>Battlefield analogue of {@link ReturnSourceCardFromGraveyardToOwnerHandEffect}. Unlike
 * {@link RegisterDelayedSelfReturnFromGraveyardEffect} the return happens right away instead of
 * being queued for a later step, and unlike {@link UndyingReturnEffect} it adds no counter and is
 * granted by a card rather than pushed by the engine.
 *
 * <p>Granted until end of turn by Abnormal Endurance ("gains 'When this creature dies, return it to
 * the battlefield tapped under its owner's control.'"). Fizzles if the card is no longer in a
 * graveyard.
 *
 * @param tapped              {@code true} to have it enter the battlefield tapped
 * @param losesAllAbilities   {@code true} to make the returned permanent lose all abilities indefinitely
 * @param overriddenCardTypes non-empty to replace the returned permanent's card types indefinitely
 */
public record ReturnSourceCardFromGraveyardToBattlefieldEffect(
        boolean tapped,
        boolean losesAllAbilities,
        Set<CardType> overriddenCardTypes)
        implements CardEffect {

    public ReturnSourceCardFromGraveyardToBattlefieldEffect {
        overriddenCardTypes = Set.copyOf(overriddenCardTypes);
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped) {
        this(tapped, false, Set.of());
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, boolean losesAllAbilities) {
        this(tapped, losesAllAbilities, Set.of());
    }

    public ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped,
                                                             Set<CardType> overriddenCardTypes) {
        this(tapped, false, overriddenCardTypes);
    }
}
