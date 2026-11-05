package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * A player takes a keyword or game action, or something happens to them: "whenever you roll one or more dice",
 * "whenever an opponent commits a crime", "whenever a player loses the game". Build it through {@link Triggers}.
 *
 * @param action what the player does or what happens to them
 * @param player who, relative to the ability's controller
 * @param filter what the card the action is taken with must be, or {@code null} for any card: only the scheme set in
 *               motion ("whenever you set a non-ongoing scheme in motion" is
 *               {@code Triggers.youSetSchemeInMotion().withFilter(nonOngoingScheme)}). The scheme collector judges it
 *               from the ability ({@code ActionAbilityReader}), so {@link #watchKey()} drops it
 */
public record PlayerActionTrigger(PlayerAction action, PlayerRelation player, CardPredicate filter)
        implements Trigger {

    public PlayerActionTrigger {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(player, "player");
        if (filter != null && action != PlayerAction.SETS_SCHEME_IN_MOTION) {
            throw new IllegalArgumentException("Only a scheme trigger takes a card filter: " + action);
        }
    }

    /** A trigger on every {@code action} of the player {@code player} names. */
    public PlayerActionTrigger(PlayerAction action, PlayerRelation player) {
        this(action, player, null);
    }

    /** This trigger narrowed to an action taken with a card that matches {@code filter}, as {@link #filter} says. */
    public PlayerActionTrigger withFilter(CardPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (this.filter != null) {
            throw new IllegalStateException("This trigger already has a filter: " + this);
        }
        return new PlayerActionTrigger(action, player, filter);
    }

    /** The trigger the collectors read this one's abilities by: without its {@link #filter}. */
    @Override
    public Trigger watchKey() {
        return filter == null ? this : new PlayerActionTrigger(action, player);
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
        COMMITS_CRIME,
        /** The player fully unlocks a Room: it gets its second unlocked designation, or both at once (CR 709.5i). */
        FULLY_UNLOCKS_ROOM
    }
}
