package com.github.laxika.magicalvibes.model.effect;

/**
 * Gifts Ungiven: "Search your library for up to four cards with different names and reveal them.
 * Target opponent chooses two of those cards. Put the chosen cards into your graveyard and the rest
 * into your hand. Then shuffle."
 *
 * <p>Resolved by {@code GiftsUngivenEffectHandler}: a
 * {@link com.github.laxika.magicalvibes.model.LibrarySearchDestination#GIFTS_UNGIVEN_POOL} library
 * search collects the revealed cards outside every zone, then the opponent's disposal choice runs
 * as a {@link com.github.laxika.magicalvibes.model.PendingPileSeparation} with
 * {@link com.github.laxika.magicalvibes.model.CardPileDisposition#GIFTS_UNGIVEN}.
 */
public record GiftsUngivenEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
