package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.FreeCastKickerOffer;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;
import java.util.UUID;

/**
 * The mana payment of a spell being cast "without paying its mana cost" whose cost increases (CR 601.2f) the caster's
 * mana pool doesn't cover: a "you may" question with the increase as its mana cost, so the caster may activate mana
 * abilities before paying (CR 601.2g, 608.2g). Paid, the paused cast goes on; declined or still unpaid, the card is
 * not cast. Engine-made only, never written on a card.
 *
 * @param cardId    the card being cast (its exile id for an exile cast)
 * @param zone      the zone it is cast from, which the cost increase is judged for
 * @param resume    which cast flow paused
 * @param exileCast the paused exile cast's options, for {@link Resume#EXILE_CAST}; {@code null} otherwise
 */
public record PayFreeCastCostIncreaseEffect(UUID cardId, Zone zone, Resume resume, FreeCastKickerOffer exileCast)
        implements CardEffect {

    /** The cast flow the payment paused. */
    public enum Resume {
        /** A single cast from exile ({@code ExileFreeCastSupport}). */
        EXILE_CAST,
        /** The next spell of the free-cast queue ({@code ExileFreeCastQueueSupport}). */
        FREE_CAST_QUEUE,
        /** A mandatory cast from a hand ({@link CastCardWithoutPayingManaCostIfAbleEffect}). */
        HAND_CAST
    }

    public PayFreeCastCostIncreaseEffect {
        Objects.requireNonNull(cardId, "cardId");
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(resume, "resume");
        if ((resume == Resume.EXILE_CAST) != (exileCast != null)) {
            throw new IllegalArgumentException("Only an exile cast carries its options");
        }
    }
}
