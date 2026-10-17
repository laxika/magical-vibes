package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Triggered effect: return a creature card that just died from a graveyard to the battlefield
 * under the ability controller's control, optionally attaching the source Equipment to it.
 *
 * <p>Nim Deathmantle's "you may pay {4}" trigger uses {@code attachSource = true}; Oathkeeper,
 * Takeno's Daisho's equipped-creature-death trigger uses {@code false} (it simply reanimates the
 * Samurai and stays unattached).
 *
 * @param dyingCardId  the card ID of the dying creature (null in card definition, filled at trigger time)
 * @param attachSource whether the source Equipment attaches to the returned creature
 * @param enterWithCounter the counter put on the returned creature, if any
 * @param enterWithCounterCount the number of counters put on the returned creature
 * @param dyingCardGraveyardEntryVersion the graveyard-entry version of the dying card when the trigger
 *                                       was collected, or null when it was not recorded
 */
public record ReturnDyingCreatureToBattlefieldEffect(UUID dyingCardId, boolean attachSource,
                                                     CounterType enterWithCounter,
                                                     int enterWithCounterCount,
                                                     Long dyingCardGraveyardEntryVersion)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToBattlefieldEffect(boolean attachSource) {
        this(null, attachSource, null, 0, null);
    }

    public ReturnDyingCreatureToBattlefieldEffect(UUID dyingCardId, boolean attachSource) {
        this(dyingCardId, attachSource, null, 0, null);
    }

    public ReturnDyingCreatureToBattlefieldEffect(boolean attachSource, CounterType enterWithCounter,
                                                  int enterWithCounterCount) {
        this(null, attachSource, enterWithCounter, enterWithCounterCount, null);
    }

    public ReturnDyingCreatureToBattlefieldEffect(UUID dyingCardId, boolean attachSource,
                                                  CounterType enterWithCounter,
                                                  int enterWithCounterCount) {
        this(dyingCardId, attachSource, enterWithCounter, enterWithCounterCount, null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToBattlefieldEffect(
                dyingCardId, attachSource, enterWithCounter, enterWithCounterCount,
                dyingCardGraveyardEntryVersion);
    }

    @Override
    public CardEffect boundToDyingCardEntryVersion(long graveyardEntryVersion) {
        return new ReturnDyingCreatureToBattlefieldEffect(
                dyingCardId, attachSource, enterWithCounter, enterWithCounterCount, graveyardEntryVersion);
    }
}
