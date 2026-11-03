package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A spell is cast (CR 601.2i), copied (CR 707.10) or countered (CR 701.6a). Build it through {@link Triggers}.
 * <p>
 * A cast trigger with a {@link SpellFilter} ("whenever you cast a creature spell", built with {@link #withFilter},
 * {@link #anySpell} and the other filter methods) is collected by the generic spell-cast collector, which judges the
 * filter and puts the ability's effects on the stack. A cast trigger without one is still collected by its effect's
 * class, as the condition-as-effect abilities ({@code NthSpellCastTriggerEffect}, {@code KickedSpellCastTriggerEffect},
 * …) and the abilities whose effect binds the spell or its caster are, until steps 14 (run 6c) and 15 of
 * effect-slot-refactor.md make them filters and event references too.
 *
 * @param event  what happens to the spell
 * @param player the player who casts or copies the spell, counters it, or controls the countered spell
 * @param filter what a cast spell must be, or {@code null} for a trigger its effect's class collects; only a
 *               {@link SpellEvent#CAST} trigger has one
 */
public record SpellTrigger(SpellEvent event, PlayerRelation player, SpellFilter filter) implements Trigger {

    public SpellTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
        if (filter != null && event != SpellEvent.CAST) {
            throw new IllegalArgumentException("Only a cast trigger has a spell filter: " + event);
        }
    }

    public SpellTrigger(SpellEvent event, PlayerRelation player) {
        this(event, player, null);
    }

    /** What happens to a spell that a {@link SpellTrigger} triggers on. */
    public enum SpellEvent {
        /** The player casts a spell: "whenever you cast a spell". */
        CAST,
        /** The player casts this spell: "when you cast this spell". */
        CAST_THIS,
        /** The player copies a spell: "whenever you copy a spell". */
        COPIED,
        /** The player counters a spell: "whenever you counter a spell". */
        COUNTERS,
        /** A spell the player controls is countered: "whenever a spell you control is countered". */
        COUNTERED,
        /** This spell is countered, or doesn't resolve because its targets are illegal. */
        THIS_COUNTERED_OR_FIZZLES
    }

    /** Whether the trigger is about this spell itself. */
    public boolean thisSpell() {
        return event == SpellEvent.CAST_THIS || event == SpellEvent.THIS_COUNTERED_OR_FIZZLES;
    }

    /**
     * A trigger about this spell functions from the stack, where the spell is as it becomes cast (CR 601.2i); any
     * other from the battlefield.
     */
    @Override
    public AbilityZones defaultZones() {
        return thisSpell() ? AbilityZones.STACK : AbilityZones.BATTLEFIELD;
    }

    /**
     * The spell-cast collectors read every cast ability by the event and player; the filter is judged by the generic
     * collector, from the {@code SpellCastTriggerEffect} the legacy view gives.
     */
    @Override
    public Trigger watchKey() {
        return filter == null ? this : new SpellTrigger(event, player);
    }

    /** This trigger for any spell, collected by the generic spell-cast collector: "whenever you cast a spell". */
    public SpellTrigger anySpell() {
        return filtered(filter -> filter);
    }

    /** This trigger for a spell that matches {@code spell} only: "whenever you cast a creature spell". */
    public SpellTrigger withFilter(CardPredicate spell) {
        return filtered(filter -> filter.withSpell(spell));
    }

    /**
     * This trigger for a spell whose stack object matches {@code stackEntry} only: "whenever you cast a spell that
     * targets this creature", "a spell from your graveyard".
     */
    public SpellTrigger withStackEntryFilter(StackEntryPredicate stackEntry) {
        return filtered(filter -> filter.withStackEntry(stackEntry));
    }

    /** This trigger for a spell cast during the ability's controller's turn only. */
    public SpellTrigger duringYourTurn() {
        return filtered(filter -> filter.withTurn(SpellFilter.Turn.YOURS));
    }

    /** This trigger for a spell cast during a turn other than the ability's controller's only. */
    public SpellTrigger duringOpponentsTurn() {
        return filtered(filter -> filter.withTurn(SpellFilter.Turn.OPPONENTS));
    }

    /** This trigger for a spell cast during a turn other than the casting player's only. */
    public SpellTrigger notDuringCastersTurn() {
        return filtered(filter -> filter.withTurn(SpellFilter.Turn.NOT_CASTERS));
    }

    /** This trigger for a spell cast during combat only. */
    public SpellTrigger duringCombat() {
        return filtered(SpellFilter::withDuringCombat);
    }

    /** This trigger for the casting player's {@code nth} spell this turn that matches the filter. */
    public SpellTrigger nthThisTurn(int nth) {
        return filtered(filter -> filter.withNthThisTurn(nth));
    }

    /** This trigger for the casting player's {@code nth} or later spell this turn that matches the filter. */
    public SpellTrigger nthOrLaterThisTurn(int nth) {
        return filtered(filter -> filter.withNthOrLaterThisTurn(nth));
    }

    /** This trigger for a spell cast with mana the ability's source produced, once for each such mana. */
    public SpellTrigger paidWithManaFromThis() {
        return filtered(SpellFilter::withPaidWithManaFromThis);
    }

    /** This trigger for a spell cast with mana a Treasure produced only. */
    public SpellTrigger paidWithTreasureMana() {
        return filtered(SpellFilter::withPaidWithTreasureMana);
    }

    /** This trigger for a spell with two or more card types only. */
    public SpellTrigger withTwoOrMoreCardTypes() {
        return filtered(SpellFilter::withTwoOrMoreCardTypes);
    }

    /** This trigger for the spell that makes its caster spend their {@code threshold}th total mana this turn. */
    public SpellTrigger expend(int threshold) {
        return filtered(filter -> filter.withExpend(threshold));
    }

    private SpellTrigger filtered(UnaryOperator<SpellFilter> change) {
        if (event != SpellEvent.CAST) {
            throw new IllegalStateException("Only a cast trigger has a spell filter: " + event);
        }
        return new SpellTrigger(event, player, change.apply(filter == null ? SpellFilter.ANY : filter));
    }
}
