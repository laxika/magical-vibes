package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FreeCastKickerOffer;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.SpellFace;
import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The mana payment of a spell being cast "without paying its mana cost" whose cost increases (CR 601.2f) the caster's
 * mana pool doesn't cover: a "you may" question with the increase as its mana cost, so the caster may activate mana
 * abilities before paying (CR 601.2g, 608.2g). Paid, the paused cast goes on; declined or still unpaid, the card is
 * not cast and goes where its cast flow puts a card it doesn't cast. Engine-made only, never written on a card.
 *
 * @param cardId the card being cast (its exile id for an exile cast)
 * @param zone   the zone it is cast from, which the cost increase is judged for
 * @param paused the cast flow that paused, with what it needs to go on
 */
public record PayFreeCastCostIncreaseEffect(UUID cardId, Zone zone, PausedCast paused) implements CardEffect {

    /** The cast flow the payment paused. */
    public sealed interface PausedCast permits ExileCast, QueueCast, HandCast, LibraryCast, RevealedCast, SplitCast,
            LibraryTopCast {
    }

    /** A single cast from exile ({@code ExileFreeCastSupport}), resumed with its options. */
    public record ExileCast(FreeCastKickerOffer offer) implements PausedCast {
        public ExileCast {
            Objects.requireNonNull(offer, "offer");
        }
    }

    /** The next spell of the free-cast queue ({@code ExileFreeCastQueueSupport}), still first in the queue. */
    public record QueueCast() implements PausedCast {
    }

    /** A mandatory cast from a hand ({@link CastCardWithoutPayingManaCostIfAbleEffect}); the card stays there. */
    public record HandCast() implements PausedCast {
    }

    /**
     * A card found or revealed by a library search and cast from there ({@code LibraryChoiceHandlerService}). It has
     * left its zone, so a card that isn't cast goes to {@code ifNotCast}.
     *
     * @param ownerId             the card's owner, whose library or hand {@code ifNotCast} names
     * @param discoverValue       the discover value of a discover cast, or null
     * @param grantHaste          whether a creature spell's permanent gains haste
     * @param sacrificeAtEndStep  whether that permanent is sacrificed at the next end step
     * @param spellManaValueLimit the largest mana value a modal spell's chosen spell may have
     * @param ifNotCast           where a card that isn't cast goes
     * @param physicalCard        the card cast, when the paused card is one of its faces (an Adventure), else null
     * @param faces               the faces the card may still be cast as (CR 716.3a, 712.8b); empty for any
     */
    public record LibraryCast(UUID ownerId, Integer discoverValue, boolean grantHaste, boolean sacrificeAtEndStep,
                              int spellManaValueLimit, IfNotCast ifNotCast, Card physicalCard,
                              Set<SpellFace> faces) implements PausedCast {
        public LibraryCast {
            Objects.requireNonNull(ownerId, "ownerId");
            Objects.requireNonNull(ifNotCast, "ifNotCast");
            faces = faces == null ? Set.of() : Set.copyOf(faces);
        }

        public LibraryCast(UUID ownerId, Integer discoverValue, boolean grantHaste, boolean sacrificeAtEndStep,
                           int spellManaValueLimit, IfNotCast ifNotCast) {
            this(ownerId, discoverValue, grantHaste, sacrificeAtEndStep, spellManaValueLimit, ifNotCast, null,
                    Set.of());
        }

        /** This cast of {@code card} as one of {@code faces}. */
        public LibraryCast castAs(Card card, Set<SpellFace> faces) {
            return new LibraryCast(ownerId, discoverValue, grantHaste, sacrificeAtEndStep, spellManaValueLimit,
                    ifNotCast, card, faces);
        }
    }

    /** Where a library-search card that isn't cast goes. */
    public enum IfNotCast {
        /** The bottom of its owner's library. */
        LIBRARY_BOTTOM,
        /** The top of its owner's library. */
        LIBRARY_TOP,
        /** Its owner's library, at a random position (it is shuffled in). */
        LIBRARY_SHUFFLED_IN,
        /** Exile, where it still is. */
        STAYS_EXILED,
        /** Its owner's hand. */
        HAND
    }

    /**
     * A revealed card held outside every zone ({@code MayCastHandlerService.castRevealedCardWithoutPaying}).
     *
     * @param ownerId          the card's owner
     * @param cardsToBottom    the other held cards, put on the bottom of the owner's library with the card if it
     *                         isn't cast; null when a card that isn't cast goes to its owner's graveyard instead
     * @param deferCompletion  whether the paused caller ends the input itself
     */
    public record RevealedCast(UUID ownerId, List<Card> cardsToBottom, boolean deferCompletion) implements PausedCast {
        public RevealedCast {
            Objects.requireNonNull(ownerId, "ownerId");
            cardsToBottom = cardsToBottom == null ? null : List.copyOf(cardsToBottom);
        }
    }

    /** A split card's chosen half, with its targets chosen, about to be put onto the stack. */
    public record SplitCast(SplitCardCast.Targeting targeting) implements PausedCast {
        public SplitCast {
            Objects.requireNonNull(targeting, "targeting");
        }
    }

    /**
     * The top card of a library played without paying its mana cost through a "you may play it" question
     * ({@link RevealTopCardMayPlayFreeEffect}); the card stays on top until it is cast.
     *
     * @param ability              the answered "you may play it" question
     * @param notPlayedDestination where a card that isn't played goes
     * @param publiclyRevealed     whether the card was revealed to every player
     */
    public record LibraryTopCast(PendingMayAbility ability, LookDestination notPlayedDestination,
                                 boolean publiclyRevealed) implements PausedCast {
        public LibraryTopCast {
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(notPlayedDestination, "notPlayedDestination");
        }
    }

    public PayFreeCastCostIncreaseEffect {
        Objects.requireNonNull(cardId, "cardId");
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(paused, "paused");
    }
}
