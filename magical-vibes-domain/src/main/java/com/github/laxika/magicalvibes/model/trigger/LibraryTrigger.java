package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;

/**
 * A player draws a card (CR 121.1), discards one (CR 701.9a) or cycles one, scries (CR 701.22a), surveils
 * (CR 701.25a), searches their library (CR 701.23a) or shuffles it (CR 701.24a). Build it through {@link Triggers}.
 *
 * @param event  what the player does
 * @param player who does it, relative to the ability's controller: the player who draws, discards, cycles, scries,
 *               surveils, searches or shuffles. For {@link LibraryEvent#THIS_DISCARDED} it is the controller of the
 *               spell or ability that causes the discard ({@link PlayerRelation#ANY} for any discard), and an event
 *               that names its player itself is {@link PlayerRelation#ANY}
 */
public record LibraryTrigger(LibraryEvent event, PlayerRelation player) implements Trigger {

    public LibraryTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
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
     * What a player does that a {@link LibraryTrigger} triggers on. Draws and discards trigger once for each card;
     * the rest once for each time the player does it.
     */
    public enum LibraryEvent {
        /** The player draws a card. */
        DRAWS,
        /** The player draws their second card of the turn. */
        DRAWS_SECOND_CARD,
        /** The player the source is attached to draws a card. */
        ENCHANTED_PLAYER_DRAWS,
        /** The player discards a card, cycling included. */
        DISCARDS,
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
