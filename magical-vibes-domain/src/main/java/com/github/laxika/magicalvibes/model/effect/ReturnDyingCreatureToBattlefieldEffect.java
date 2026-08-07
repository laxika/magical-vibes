package com.github.laxika.magicalvibes.model.effect;

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
 */
public record ReturnDyingCreatureToBattlefieldEffect(UUID dyingCardId, boolean attachSource)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToBattlefieldEffect(boolean attachSource) {
        this(null, attachSource);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToBattlefieldEffect(dyingCardId, attachSource);
    }
}
