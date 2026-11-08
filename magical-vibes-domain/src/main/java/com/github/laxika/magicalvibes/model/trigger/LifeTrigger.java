package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * A player gains life, loses life or pays life (CR 119.3, 119.4). Damage dealt to a player is a loss of life
 * (CR 120.3a), and a player who gains 0 life has no life gain event, so a "whenever [a player] gains life" ability
 * doesn't trigger (CR 119.9). Build it through {@link Triggers}.
 *
 * @param event         what happens to the player's life total
 * @param player        whose life total it is, relative to the ability's controller
 * @param firstEachTurn whether only the player's first such event each turn counts, the events before the source was
 *                      on the battlefield included: "whenever you gain life for the first time each turn" is {@code
 *                      Triggers.youGainLife().firstTimeEachTurn()}. A count of the player's events, not a limit on the
 *                      ability's triggers
 * @param spellFilter   what the spell that causes the life gain must be, or {@code null} for any source: "whenever a
 *                      white instant or sorcery spell causes you to gain life" is {@code
 *                      Triggers.youGainLife().causedBySpell(...)}. The life gain's source is a spell when the spell
 *                      itself gains the life as it resolves, its lifelink damage included; a triggered or activated
 *                      ability of the card isn't one. Only {@link LifeEvent#GAINS} takes one. The collectors judge
 *                      it and {@link #firstEachTurn} from the ability ({@code LifeAbilityReader}), so
 *                      {@link #watchKey()} drops them
 */
public record LifeTrigger(LifeEvent event, PlayerRelation player, boolean firstEachTurn, CardPredicate spellFilter)
        implements Trigger {

    public LifeTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
        if (firstEachTurn && event == LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN) {
            throw new IllegalArgumentException("The event already counts the player's first loss: " + event);
        }
        if (spellFilter != null && event != LifeEvent.GAINS) {
            throw new IllegalArgumentException("Only a life gain trigger takes a spell filter: " + event);
        }
    }

    /** A trigger on every such event, or only the player's first each turn, from any source. */
    public LifeTrigger(LifeEvent event, PlayerRelation player, boolean firstEachTurn) {
        this(event, player, firstEachTurn, null);
    }

    /** A trigger on every such event. */
    public LifeTrigger(LifeEvent event, PlayerRelation player) {
        this(event, player, false, null);
    }

    /**
     * This trigger narrowed to the player's first such event each turn, as {@link #firstEachTurn} says: "whenever you
     * lose life for the first time each turn".
     */
    public LifeTrigger firstTimeEachTurn() {
        if (firstEachTurn) {
            throw new IllegalStateException("This life trigger already counts its events: " + this);
        }
        return new LifeTrigger(event, player, true, spellFilter);
    }

    /**
     * This trigger narrowed to a life gain a spell matching {@code spellFilter} causes, as {@link #spellFilter} says:
     * "whenever a white instant or sorcery spell causes you to gain life".
     */
    public LifeTrigger causedBySpell(CardPredicate spellFilter) {
        Objects.requireNonNull(spellFilter, "spellFilter");
        if (this.spellFilter != null) {
            throw new IllegalStateException("This life trigger already has a spell filter: " + this);
        }
        return new LifeTrigger(event, player, firstEachTurn, spellFilter);
    }

    /**
     * The trigger the {@code LifeWatch} collectors read this one's abilities by: without {@link #firstEachTurn} and
     * {@link #spellFilter}, and,
     * for {@link LifeEvent#LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN}, the loss of life of the same player, whose
     * collector judges the rest from the ability ({@code LifeAbilityReader}).
     */
    @Override
    public Trigger watchKey() {
        if (event == LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN) {
            return new LifeTrigger(LifeEvent.LOSES, player);
        }
        return firstEachTurn || spellFilter != null ? new LifeTrigger(event, player) : this;
    }

    /** What happens to a player's life total. Each value triggers once for each life gain, loss or payment event. */
    public enum LifeEvent {
        /** The player gains one or more life. */
        GAINS,
        /** The player loses one or more life, from damage or otherwise. */
        LOSES,
        /**
         * The player loses one or more life during their own turn, for the first time that turn, counting the losses
         * before the source was on the battlefield: "whenever an opponent loses life for the first time during each
         * of their turns".
         */
        LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN,
        /** The player pays one or more life (CR 119.4). */
        PAYS
    }
}
