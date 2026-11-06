package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.Objects;

/**
 * A player activates an ability, or a mana ability resolves (CR 605). Build it through {@link Triggers}.
 *
 * @param event  which abilities it triggers on
 * @param player the player who activates the ability
 * @param source what the permanent whose ability is activated must be ("an ability of a creature", "of an artifact"),
 *               or {@code null} for any
 * @param kind   what kind of activated ability it must be ("a loyalty ability", "a ninjutsu ability"), or
 *               {@code null} for any
 * @param stackEntry what the activated ability on the stack must be, with its targets ("that targets only Bill
 *               Potts"), or {@code null} for any; see {@link #withStackEntryFilter}
 */
public record AbilityTrigger(AbilityEvent event, PlayerRelation player, PermanentPredicate source, AbilityKind kind,
                             StackEntryPredicate stackEntry)
        implements Trigger {

    public AbilityTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
    }

    public AbilityTrigger(AbilityEvent event, PlayerRelation player) {
        this(event, player, null, null, null);
    }

    /** This trigger for an ability of a permanent that matches {@code source} only. */
    public AbilityTrigger withSource(PermanentPredicate source) {
        return new AbilityTrigger(event, player, Objects.requireNonNull(source, "source"), kind, stackEntry);
    }

    /** This trigger for an activated ability of {@code kind} only. */
    public AbilityTrigger withKind(AbilityKind kind) {
        return new AbilityTrigger(event, player, source, Objects.requireNonNull(kind, "kind"), stackEntry);
    }

    /**
     * This trigger for an activated ability whose stack entry matches {@code stackEntry} only, judged with the
     * ability's targets once it is on the stack ("an ability that targets only Bill Potts"). Only the collector of the
     * abilities that copy the activated ability ({@code CopyTriggeringSpellOrAbilityEffect}) reads it, as it collects
     * them once the activated ability is on the stack.
     */
    public AbilityTrigger withStackEntryFilter(StackEntryPredicate stackEntry) {
        return new AbilityTrigger(event, player, source, kind, Objects.requireNonNull(stackEntry, "stackEntry"));
    }

    /**
     * This trigger without its filters: the trigger the source, kind and stack-entry filter narrow. The activation
     * collectors read every ability of an event by this trigger, and judge the filters themselves.
     */
    public AbilityTrigger unfiltered() {
        return source == null && kind == null && stackEntry == null ? this : new AbilityTrigger(event, player);
    }

    /** The activations an {@link AbilityTrigger} triggers on. */
    public enum AbilityEvent {
        /** Any activated ability, mana abilities included: "whenever you activate an ability". */
        ACTIVATED,
        /** An activated ability that isn't a mana ability. */
        NONMANA_ACTIVATED,
        /** An exhaust ability. */
        EXHAUST_ACTIVATED,
        /** An embalm or eternalize ability. */
        ETERNALIZE_OR_EMBALM_ACTIVATED,
        /** A mana ability of this permanent resolves: "whenever a mana ability of this creature resolves". */
        THIS_MANA_ABILITY_RESOLVES
    }

    /** A kind of activated ability an {@link AbilityTrigger} can be restricted to. */
    public enum AbilityKind {
        /** An ability with a loyalty cost: "whenever you activate a loyalty ability". */
        LOYALTY,
        /** A ninjutsu ability. */
        NINJUTSU,
        /** An outlast ability. */
        OUTLAST,
        /** A power-up ability. */
        POWER_UP,
        /** A boast ability. */
        BOAST,
        /** An ability with {T} in its activation cost. */
        WITH_TAP_COST,
        /** An ability without {T} in its activation cost. */
        WITHOUT_TAP_COST;

        /** Whether {@code ability}, the activated ability, is of this kind. */
        public boolean matches(ActivatedAbility ability) {
            return switch (this) {
                case LOYALTY -> ability.getLoyaltyCost() != null;
                case NINJUTSU -> ability.isNinjutsuAbility();
                case OUTLAST -> ability.isOutlastAbility();
                case POWER_UP -> ability.isPowerUpAbility();
                case BOAST -> ability.isBoast();
                case WITH_TAP_COST -> ability.isRequiresTap();
                case WITHOUT_TAP_COST -> !ability.isRequiresTap();
            };
        }
    }
}
