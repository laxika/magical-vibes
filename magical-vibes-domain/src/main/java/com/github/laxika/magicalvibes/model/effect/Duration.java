package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * How long an effect lasts. An effect a resolving spell or ability makes lasts as long as the spell or ability states,
 * and to the end of the game if it states nothing (CR 611.2a): it ends at an event ({@link #end}), or it lasts "for as
 * long as" something holds ({@link #asLongAs}, CR 611.2b) and ends for good once that stops holding. An effect of a
 * static ability lasts as long as the ability does ({@link #STATIC}).
 *
 * <p>The named constants are the durations cards write; every duration is one of them.
 *
 * @param end      the event that ends the effect; {@link End#NEVER} for one that lasts to the end of the game or for
 *                 as long as {@link #asLongAs} holds
 * @param asLongAs what a "for as long as" duration holds while, or {@code null} for a duration that ends at an event
 */
public record Duration(End end, AsLongAs asLongAs) {

    /** The event that ends an effect. */
    public enum End {
        /** Nothing ends it: it lasts to the end of the game (CR 611.2a) or for as long as its condition holds. */
        NEVER,
        /** The cleanup step: "until end of turn" and "this turn" effects end there (CR 514.2). */
        END_OF_TURN,
        /** The beginning of the next end step. */
        NEXT_END_STEP,
        /** The end of the combat phase. */
        END_OF_COMBAT,
        /** The beginning of its controller's next turn. */
        YOUR_NEXT_TURN,
        /** The cleanup step of its controller's next turn. */
        END_OF_YOUR_NEXT_TURN,
        /** The next untap step of the affected permanent's controller, not the effect's controller. */
        AFFECTED_CONTROLLERS_NEXT_UNTAP_STEP,
        /** The beginning of its controller's next upkeep. */
        YOUR_NEXT_UPKEEP,
        /** The end of its controller's next upkeep. */
        END_OF_YOUR_NEXT_UPKEEP,
        /** Any player casting a creature spell. */
        CREATURE_SPELL_CAST,
        /** Its controller casting a spell its effect matches. */
        MATCHING_SPELL_CAST,
        /** The player its effect names casting a spell its effect matches. */
        TARGET_PLAYER_MATCHING_SPELL_CAST,
        /** Its source card being cast from exile. */
        SOURCE_CARD_CAST_FROM_EXILE,
        /** A player planeswalking. */
        PLANESWALK
    }

    /** What a "for as long as" duration holds while (CR 611.2b). */
    public enum AsLongAs {
        /**
         * Its source stays on the battlefield; a control effect, and an effect that says "for as long as you control"
         * it, also only while its creator controls the source (Olivia Voldaren).
         */
        SOURCE_ON_BATTLEFIELD,
        /** Its source stays on the battlefield and its creator controls it ("for as long as you control this Saga"). */
        SOURCE_CONTROLLED,
        /** Its source stays on the battlefield, whoever controls it ("for as long as this creature remains"). */
        SOURCE_REMAINS,
        /** Its source stays on the battlefield, under its creator's control, and tapped (Seasinger). */
        SOURCE_TAPPED,
        /** Its source stays tapped, whoever controls it (Vedalken Shackles). */
        SOURCE_REMAINS_TAPPED,
        /** Its source, an Aura or Equipment, stays attached to the affected permanent. */
        ATTACHED,
        /** Its source card stays in exile (teach); it ends as the card leaves exile, cast or not. */
        SOURCE_CARD_EXILED,
        /** The static ability that generates it applies (CR 611.3). */
        STATIC_ABILITY;

        /** Whether it holds only while its source permanent is on the battlefield: it ends as the source leaves. */
        public boolean needsSourceOnBattlefield() {
            return this != SOURCE_CARD_EXILED && this != STATIC_ABILITY;
        }

        /** Whether it holds only while its source stays tapped: it ends as the source untaps. */
        public boolean needsSourceTapped() {
            return this == SOURCE_TAPPED || this == SOURCE_REMAINS_TAPPED;
        }
    }

    /** No stated duration: it lasts to the end of the game (CR 611.2a), "becomes a creature permanently". */
    public static final Duration PERMANENT = new Duration(End.NEVER, null);
    /** "Until end of turn" (CR 514.2). */
    public static final Duration UNTIL_END_OF_TURN = new Duration(End.END_OF_TURN, null);
    /** Until the beginning of the next end step. */
    public static final Duration UNTIL_NEXT_END_STEP = new Duration(End.NEXT_END_STEP, null);
    /** "Until end of combat" (Jade Statue). */
    public static final Duration UNTIL_END_OF_COMBAT = new Duration(End.END_OF_COMBAT, null);
    /** "Until your next turn". */
    public static final Duration UNTIL_YOUR_NEXT_TURN = new Duration(End.YOUR_NEXT_TURN, null);
    /** "Until the end of your next turn". */
    public static final Duration UNTIL_END_OF_YOUR_NEXT_TURN = new Duration(End.END_OF_YOUR_NEXT_TURN, null);
    /** "Until its controller's next untap step" (Orcish Farmer), keyed to the affected permanent's controller. */
    public static final Duration UNTIL_CONTROLLERS_NEXT_UNTAP_STEP =
            new Duration(End.AFFECTED_CONTROLLERS_NEXT_UNTAP_STEP, null);
    /** "Until your next upkeep" (Cycle of Life). */
    public static final Duration UNTIL_CONTROLLERS_NEXT_UPKEEP = new Duration(End.YOUR_NEXT_UPKEEP, null);
    /** Through the end of its controller's next upkeep. */
    public static final Duration UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP =
            new Duration(End.END_OF_YOUR_NEXT_UPKEEP, null);
    /** Until any player casts a creature spell. */
    public static final Duration UNTIL_CREATURE_SPELL_CAST = new Duration(End.CREATURE_SPELL_CAST, null);
    /** Until its controller casts a spell its effect matches. */
    public static final Duration UNTIL_MATCHING_SPELL_CAST = new Duration(End.MATCHING_SPELL_CAST, null);
    /** Until the player its effect names casts a spell its effect matches. */
    public static final Duration UNTIL_TARGET_PLAYER_MATCHING_SPELL_CAST =
            new Duration(End.TARGET_PLAYER_MATCHING_SPELL_CAST, null);
    /** Until its source card is cast from exile. */
    public static final Duration UNTIL_SOURCE_CARD_CAST_FROM_EXILE =
            new Duration(End.SOURCE_CARD_CAST_FROM_EXILE, null);
    /** Until a player planeswalks. */
    public static final Duration UNTIL_PLANESWALK = new Duration(End.PLANESWALK, null);
    /** For as long as its source is on the battlefield (Awakener Druid); see {@link AsLongAs#SOURCE_ON_BATTLEFIELD}. */
    public static final Duration WHILE_SOURCE_ON_BATTLEFIELD = new Duration(End.NEVER, AsLongAs.SOURCE_ON_BATTLEFIELD);
    /** "For as long as you control this Saga". */
    public static final Duration WHILE_SOURCE_CONTROLLED = new Duration(End.NEVER, AsLongAs.SOURCE_CONTROLLED);
    /** "For as long as this creature remains on the battlefield" (Infernal Denizen). */
    public static final Duration WHILE_SOURCE_REMAINS = new Duration(End.NEVER, AsLongAs.SOURCE_REMAINS);
    /** For as long as you control its source and it remains tapped (Seasinger). */
    public static final Duration WHILE_SOURCE_TAPPED = new Duration(End.NEVER, AsLongAs.SOURCE_TAPPED);
    /** For as long as its source remains tapped (Vedalken Shackles). */
    public static final Duration WHILE_SOURCE_REMAINS_TAPPED =
            new Duration(End.NEVER, AsLongAs.SOURCE_REMAINS_TAPPED);
    /** For as long as its source card remains exiled (teach). */
    public static final Duration WHILE_SOURCE_CARD_EXILED = new Duration(End.NEVER, AsLongAs.SOURCE_CARD_EXILED);
    /** For as long as its source, an Aura or Equipment, stays attached to the affected permanent. */
    public static final Duration WHILE_ATTACHED = new Duration(End.NEVER, AsLongAs.ATTACHED);
    /** The effect of a static ability (CR 611.3): Auras, Equipment, Stations. */
    public static final Duration STATIC = new Duration(End.NEVER, AsLongAs.STATIC_ABILITY);

    public Duration {
        Objects.requireNonNull(end, "end");
        if (asLongAs != null && end != End.NEVER) {
            throw new IllegalArgumentException("A \"for as long as\" duration ends at no event: " + end);
        }
    }
}
