package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Distinct-name library search that reveals up to four cards, then sends two chosen cards to the
 * controller's graveyard and the rest to their hand or onto the battlefield tapped. The optional
 * predicate narrows the searched cards, and the target flag distinguishes Gifts Ungiven's targeted
 * opponent from Realms Uncharted and Elemental Teachings' non-targeted opponent choice.
 *
 * <p>Resolved by {@code GiftsUngivenEffectHandler}: a
 * {@link com.github.laxika.magicalvibes.model.LibrarySearchDestination#GIFTS_UNGIVEN_POOL} library
 * search collects the revealed cards outside every zone, then the opponent's disposal choice runs
 * as a {@link com.github.laxika.magicalvibes.model.PendingPileSeparation} with
 * the configured {@link com.github.laxika.magicalvibes.model.CardPileDisposition}.
 */
public record GiftsUngivenEffect(CardPredicate filterPredicate, boolean targetOpponent,
                                 CardPileDisposition disposition) implements CardEffect {

    public GiftsUngivenEffect() {
        this(null, true, CardPileDisposition.GIFTS_UNGIVEN);
    }

    public GiftsUngivenEffect(CardPredicate filterPredicate) {
        this(filterPredicate, true, CardPileDisposition.GIFTS_UNGIVEN);
    }

    public GiftsUngivenEffect(CardPredicate filterPredicate, boolean targetOpponent) {
        this(filterPredicate, targetOpponent, CardPileDisposition.GIFTS_UNGIVEN);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetOpponent ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
