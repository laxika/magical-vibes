package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile creature cards and create a 2/2 black Zombie token for each card exiled this way.
 *
 * <p>Two modes:
 * <ul>
 *   <li>{@code targetPlayerGraveyard = false} (default): exile the effect's {@code targetCardIds}
 *       (individual creature cards chosen at cast time) — Midnight Ritual's "exile X target creature
 *       cards from a single graveyard".</li>
 *   <li>{@code targetPlayerGraveyard = true}: exile ALL creature cards from the single targeted
 *       player's graveyard — Necromancer's Covenant's "exile all creature cards from target player's
 *       graveyard".</li>
 * </ul>
 *
 * @param targetPlayerGraveyard whether the effect exiles a target player's whole graveyard (creature
 *                              cards only) instead of individually targeted cards
 */
public record ExileCreaturesFromGraveyardAndCreateTokensEffect(boolean targetPlayerGraveyard) implements CardEffect {

    public ExileCreaturesFromGraveyardAndCreateTokensEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPlayerGraveyard ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
