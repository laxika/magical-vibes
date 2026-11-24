package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/**
 * Timmerian Fiends' activated ante ability: "The owner of target artifact may ante the top card of
 * their library. If that player doesn't, exchange ownership of that artifact and Timmerian Fiends.
 * Put the artifact card into your graveyard and Timmerian Fiends from anywhere into that player's
 * graveyard."
 *
 * <p>The targeted artifact's owner is the decision maker. Anteing is modelled as the observable
 * single-game zone movement — the top card of that player's library leaves the game (moved to
 * exile), mirroring {@link AmuletOfQuozAnteEffect}. An owner with an empty library has no top card
 * to ante, so they can't ante and the exchange happens immediately.
 *
 * <p>The oracle "exchange ownership … This change in ownership is permanent" is modeled within one
 * game, as {@link TempestEfreetAnteExchangeEffect} does: the artifact card goes into the ability
 * controller's graveyard and Timmerian Fiends into the artifact owner's graveyard, and the two cards
 * swap owners in {@code GameData.cardOwnerOverrides}. The cross-game transfer an ante implies is not
 * modeled.
 *
 * <p>Targets the artifact — the ability declares the legal targets through a permanent predicate
 * target filter, and {@link #targetSpec()} narrows the harmful permanent category to artifacts.
 */
public record TimmerianFiendsAnteExchangeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentIsArtifactPredicate());
    }
}
