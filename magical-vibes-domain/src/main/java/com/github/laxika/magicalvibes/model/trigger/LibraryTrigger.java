package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * A player draws a card (CR 121.1), discards one (CR 701.9a) or cycles one, scries (CR 701.22a), surveils
 * (CR 701.25a), searches their library (CR 701.23a) or shuffles it (CR 701.24a). Build it through {@link Triggers}.
 *
 * @param event     what the player does
 * @param player    who does it, relative to the ability's controller: the player who draws, discards, cycles, scries,
 *                  surveils, searches or shuffles. For {@link LibraryEvent#THIS_DISCARDED} it is the controller of the
 *                  spell or ability that causes the discard ({@link PlayerRelation#ANY} for any discard), and an event
 *                  that names its player itself is {@link PlayerRelation#ANY}
 * @param qualifier what the discarded card must be, or {@code null} for any card: "whenever you discard a creature
 *                  card" is {@code Triggers.youDiscardCard().withFilter(new CardTypePredicate(CardType.CREATURE))}.
 *                  For "one or more cards" it is enough that one of them matches. The collectors judge it from the
 *                  ability ({@code LibraryAbilityReader}), so {@link #watchKey()} drops it
 * @param nth       which of the player's events of this kind this turn triggers the ability, or {@code 0} for every
 *                  one: "whenever you draw your second card each turn" is {@code 2}, "whenever you cycle a card for
 *                  the first time each turn" {@code 1}. The count includes the events before the source was on the
 *                  battlefield. With a {@link #qualifier} it counts the matching events ("whenever you discard one or
 *                  more nonland cards for the first time each turn"). The collectors judge it from the ability
 *                  ({@code LibraryAbilityReader}), so {@link #watchKey()} drops it
 */
public record LibraryTrigger(LibraryEvent event, PlayerRelation player, CardPredicate qualifier, int nth)
        implements Trigger {

    public LibraryTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
        if (nth < 0) {
            throw new IllegalArgumentException("nth must not be negative: " + nth);
        }
    }

    /** A trigger on every such event, whatever the card. */
    public LibraryTrigger(LibraryEvent event, PlayerRelation player) {
        this(event, player, null, 0);
    }

    /**
     * This trigger narrowed to a discarded card that matches {@code filter}, as {@link #qualifier} says: "whenever you
     * discard a land card".
     */
    public LibraryTrigger withFilter(CardPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (qualifier != null) {
            throw new IllegalStateException("This library trigger already has a filter: " + this);
        }
        return new LibraryTrigger(event, player, filter, nth);
    }

    /**
     * This trigger narrowed to the player's first such event each turn, as {@link #nth} says: "whenever you cycle a
     * card for the first time each turn".
     */
    public LibraryTrigger firstTimeEachTurn() {
        return nthTimeEachTurn(1);
    }

    /**
     * This trigger narrowed to the player's {@code nth} such event each turn, as {@link #nth} says: "whenever you draw
     * your third card each turn".
     */
    public LibraryTrigger nthTimeEachTurn(int nth) {
        if (nth < 1) {
            throw new IllegalArgumentException("nth must be positive: " + nth);
        }
        if (this.nth != 0) {
            throw new IllegalStateException("This library trigger already counts its events: " + this);
        }
        return new LibraryTrigger(event, player, qualifier, nth);
    }

    /**
     * Whether the trigger is about this card itself being cycled or discarded rather than about a permanent's
     * controller or an opponent.
     */
    public boolean thisCard() {
        return event == LibraryEvent.THIS_CYCLED || event == LibraryEvent.THIS_DISCARDED;
    }

    /**
     * A trigger about this card being cycled or discarded triggers from whatever zone the card winds up in
     * (CR 702.29c): its owner's graveyard (CR 701.9a), or the exile or library a replacement effect sends it to; any
     * other from the battlefield.
     */
    @Override
    public AbilityZones defaultZones() {
        return thisCard() ? AbilityZones.of(Zone.GRAVEYARD, Zone.EXILE, Zone.LIBRARY) : AbilityZones.BATTLEFIELD;
    }

    /**
     * The library, draw and discard collectors read this trigger's abilities themselves ({@code LibraryAbilityReader}):
     * the discarded card's filter, the count of the player's events this turn, the event a collector of another event
     * reads and the ability's options come from the ability, so it has no legacy view.
     */
    @Override
    public boolean collectedAsAbility() {
        return true;
    }

    /**
     * The trigger the {@code LibraryWatch} collectors read this one's abilities by: without its {@link #qualifier} and
     * {@link #nth}, and, for the events no collector of their own reads, with the event whose collector reads them.
     * "You cycle a card" and "an opponent's spell or ability causes you to discard a card" are read by the "you discard
     * a card" collector, and a draw in or out of a draw step by the collector of every draw. Those collectors judge the
     * ability's own event ({@code LibraryAbilityReader}).
     */
    @Override
    public Trigger watchKey() {
        LibraryEvent watched = watchedEvent();
        if (watched == event && qualifier == null && nth == 0) {
            return this;
        }
        return new LibraryTrigger(watched, player);
    }

    /** The event of {@link #watchKey()}: {@link #event}, or the event whose collector reads this one's abilities. */
    public LibraryEvent watchedEvent() {
        return switch (event) {
            case CYCLES -> player == PlayerRelation.YOU ? LibraryEvent.DISCARDS : event;
            case DISCARDS_BY_OPPONENT -> LibraryEvent.DISCARDS;
            case DRAWS_EXCEPT_FIRST_IN_DRAW_STEP, DRAWS_FIRST_IN_DRAW_STEP -> LibraryEvent.DRAWS;
            default -> event;
        };
    }

    /**
     * What a player does that a {@link LibraryTrigger} triggers on. Draws and discards trigger once for each card;
     * the rest once for each time the player does it.
     */
    public enum LibraryEvent {
        /** The player draws a card. */
        DRAWS,
        /** The player draws a card, except the first one they draw in each of their draw steps. */
        DRAWS_EXCEPT_FIRST_IN_DRAW_STEP,
        /** The player draws their first card during one of their draw steps. */
        DRAWS_FIRST_IN_DRAW_STEP,
        /** The player the source is attached to draws a card. */
        ENCHANTED_PLAYER_DRAWS,
        /** The player discards a card, cycling included. */
        DISCARDS,
        /** A spell or ability an opponent of the player controls causes the player to discard a card. */
        DISCARDS_BY_OPPONENT,
        /** The player discards one or more cards at once, once for each such discard. */
        DISCARDS_ONE_OR_MORE,
        /** The player cycles a card. */
        CYCLES,
        /** The player cycles this card: discards it to pay a cycling cost (CR 702.29c). */
        THIS_CYCLED,
        /** This card is discarded, by a spell or ability the player controls. */
        THIS_DISCARDED,
        /** The player scries. */
        SCRIES,
        /** The player surveils. */
        SURVEILS,
        /** The player searches their own library, also when nothing is found. */
        SEARCHES_OWN_LIBRARY,
        /** The player shuffles their library. */
        SHUFFLES_LIBRARY,
        /** A spell or ability causes its controller to shuffle their library. */
        SPELL_OR_ABILITY_CAUSES_ITS_CONTROLLER_TO_SHUFFLE
    }
}
