package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * A player takes a keyword or game action, or something happens to them: "whenever you roll one or more dice",
 * "whenever an opponent commits a crime", "whenever a player loses the game". Build it through {@link Triggers}.
 *
 * @param action what the player does or what happens to them
 * @param player who, relative to the ability's controller
 */
public record PlayerActionTrigger(PlayerAction action, PlayerRelation player) implements Trigger {

    public PlayerActionTrigger {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(player, "player");
    }

    /** What a player does, or what happens to them. */
    public enum PlayerAction {
        /** The player sets a scheme in motion (Archenemy). */
        SETS_SCHEME_IN_MOTION,
        /** The player gives a gift. */
        GIVES_GIFT,
        /** The player solves a Case. */
        SOLVES_CASE,
        /** The player foretells a card. */
        FORETELLS,
        /** The player forages. */
        FORAGES,
        /** The players finish a vote. */
        FINISH_VOTING,
        /**
         * The Ring tempts the player and they choose their Ring-bearer. Kept apart from {@link #TEMPTED_BY_RING}
         * because the two tempt effects of the engine each fire one of them.
         */
        CHOOSES_RING_BEARER,
        /** The Ring tempts the player. */
        TEMPTED_BY_RING,
        /** The player investigates for the first time this turn. */
        INVESTIGATES_FIRST_TIME_EACH_TURN,
        /** The player investigates one or more times. */
        INVESTIGATES,
        /** The player manifests dread. */
        MANIFESTS_DREAD,
        /** The player completes a dungeon. */
        COMPLETES_DUNGEON,
        /** The player rolls one or more dice: once for each roll. */
        ROLLS_ONE_OR_MORE_DICE,
        /** The player rolls a die: once for each die. */
        ROLLS_DIE,
        /** A die the player rolls shows its highest natural result: once for each such die. */
        ROLLS_HIGHEST_NATURAL_RESULT,
        /** The player rolls a natural 20. */
        ROLLS_NATURAL_20,
        /** The player waterbends, earthbends, firebends or airbends. */
        BENDS,
        /** The player exerts a creature. */
        EXERTS,
        /** The player collects evidence. */
        COLLECTS_EVIDENCE,
        /** The player discovers. */
        DISCOVERS,
        /** The player seeks one or more cards. */
        SEEKS,
        /** The player conjures one or more cards. */
        CONJURES,
        /** The player becomes the monarch. */
        BECOMES_MONARCH,
        /** The player wins a coin flip. */
        WINS_COIN_FLIP,
        /** The player loses a coin flip. */
        LOSES_COIN_FLIP,
        /** The player pays a mana tax that a spell or permanent of the ability's controller imposes. */
        PAYS_TAX,
        /** The player clashes. */
        CLASHES,
        /** The player loses the game. */
        LOSES_GAME,
        /** The player commits a crime (CR 700.13). */
        COMMITS_CRIME
    }
}
