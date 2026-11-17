package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Creates a delayed triggered ability (CR 603.7a) of the resolving spell or ability's controller that triggers when
 * they cast a spell matching {@code spellFilter} (any spell when {@code null}) and {@code stackEntryFilter} (none when
 * {@code null}), putting {@code resolvedEffects} on the stack as one object, for {@code duration}: "until end of turn,
 * whenever you cast a black spell, ..." (Mountain Titan), "when you next cast a creature spell this turn, ..."
 * (Vivien, Monsters' Advocate), "until your next turn, whenever you cast an instant or sorcery spell, ..." (Nightmares
 * and Daydreams) or a one-time boon (Swiftspear's Teachings). Its source is the maker's source (CR 603.7e), and it
 * triggers whether or not that source is still on the battlefield, only unable to affect it once it has left
 * (CR 603.7c).
 *
 * @param spellFilter      which cast spells trigger it; {@code null} = any spell
 * @param stackEntryFilter what the cast spell's stack object must match; {@code null} = anything
 * @param resolvedEffects  the ability's effects
 * @param duration         how long it lasts (CR 603.7b)
 * @param once             whether it triggers for the next matching spell only, instead of for every one
 * @param targetFilter     what its targets, chosen as it goes on the stack (CR 603.3d), must match; {@code null} = the
 *                         effects' own
 */
public record RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                               StackEntryPredicate stackEntryFilter,
                                                               List<CardEffect> resolvedEffects,
                                                               Duration duration,
                                                               boolean once,
                                                               TargetFilter targetFilter)
        implements CardEffect {

    /**
     * The delayed ability {@code ability} of {@code controllerId} that lasts for {@code duration} and, when
     * {@code once}, triggers for the next matching spell only (CR 603.7b), from a resolution of an object with the
     * source {@code sourceCard} and {@code sourcePermanentId} (or none) on turn {@code turnNumber}, whose targets must
     * match {@code targetFilter} (or the effects' own when {@code null}). The durations a delayed spell-cast ability
     * has are {@link Duration#UNTIL_END_OF_TURN}, {@link Duration#UNTIL_YOUR_NEXT_TURN} and, for a one-time boon,
     * {@link Duration#PERMANENT} with {@code once}.
     */
    static TemporaryGlobalTriggeredAbility delayed(Duration duration, boolean once, UUID controllerId,
                                                   Card sourceCard, TriggeredAbility ability,
                                                   UUID sourcePermanentId, int turnNumber, TargetFilter targetFilter) {
        TemporaryGlobalTriggeredAbility delayed;
        if (duration.equals(Duration.UNTIL_END_OF_TURN)) {
            delayed = once
                    ? TemporaryGlobalTriggeredAbility.nextThisTurn(controllerId, sourceCard, ability, sourcePermanentId)
                    : TemporaryGlobalTriggeredAbility.thisTurn(controllerId, sourceCard, ability, sourcePermanentId);
        } else if (duration.equals(Duration.UNTIL_YOUR_NEXT_TURN) && !once) {
            delayed = TemporaryGlobalTriggeredAbility.untilYourNextTurn(controllerId, sourceCard, ability,
                    sourcePermanentId, turnNumber);
        } else if (duration.equals(Duration.PERMANENT) && once) {
            delayed = TemporaryGlobalTriggeredAbility.boon(controllerId, sourceCard, ability, sourcePermanentId);
        } else {
            throw new IllegalArgumentException("No delayed spell-cast ability lasts " + duration
                    + (once ? ", once" : ""));
        }
        return targetFilter == null ? delayed : new TemporaryGlobalTriggeredAbility(delayed.controllerId(),
                delayed.sourceCard(), delayed.ability(), targetFilter, delayed.untilEndOfNextTurn(),
                delayed.untilNextTurn(), delayed.registrationTurnNumber(), delayed.expirationPlayerId(),
                delayed.watchedPermanentId(), delayed.sourcePermanentId(), delayed.watchedPlayerId(),
                delayed.linkedExiledCardId(), delayed.untilTriggered(), delayed.next(), delayed.boon());
    }

    /**
     * The delayed triggered ability this makes of {@code controllerId}, from a resolution of an object with the source
     * {@code sourceCard} and {@code sourcePermanentId} (or none) on turn {@code turnNumber}.
     */
    public TemporaryGlobalTriggeredAbility delayed(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                                   int turnNumber) {
        return delayed(duration, once, controllerId, sourceCard, delayedAbility(), sourcePermanentId, turnNumber,
                targetFilter);
    }

    /** "Until end of turn, whenever you cast a [spellFilter] spell, [effects]." */
    public static RegisterDelayedControllerSpellCastTriggerEffect thisTurn(CardPredicate spellFilter,
                                                                          List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.UNTIL_END_OF_TURN, false, null);
    }

    /** "When you next cast a [spellFilter] spell this turn, [effects]." */
    public static RegisterDelayedControllerSpellCastTriggerEffect nextThisTurn(CardPredicate spellFilter,
                                                                              List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.UNTIL_END_OF_TURN, true, null);
    }

    /** "Until your next turn, whenever you cast a [spellFilter] spell, [effects]." */
    public static RegisterDelayedControllerSpellCastTriggerEffect untilYourNextTurn(CardPredicate spellFilter,
                                                                                   List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.UNTIL_YOUR_NEXT_TURN, false, null);
    }

    /** "You get a one-time boon with 'When you cast a [spellFilter] spell, [effects].'" */
    public static RegisterDelayedControllerSpellCastTriggerEffect boon(CardPredicate spellFilter,
                                                                      List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.PERMANENT, true, null);
    }

    /** This registration, triggering only for a spell whose stack object matches {@code filter} too. */
    public RegisterDelayedControllerSpellCastTriggerEffect withStackEntryFilter(StackEntryPredicate filter) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, filter, resolvedEffects, duration,
                once, targetFilter);
    }

    /** This registration, its ability's targets restricted to {@code filter}. */
    public RegisterDelayedControllerSpellCastTriggerEffect withTargetFilter(TargetFilter filter) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, stackEntryFilter, resolvedEffects,
                duration, once, filter);
    }

    /** The delayed triggered ability this creates: "whenever you cast" a matching spell, its effects. */
    public TriggeredAbility delayedAbility() {
        return castAbility(spellFilter, stackEntryFilter, resolvedEffects);
    }

    /** "Whenever you cast" a spell matching {@code spellFilter} and {@code stackEntryFilter}, {@code effects}. */
    static TriggeredAbility castAbility(CardPredicate spellFilter, StackEntryPredicate stackEntryFilter,
                                        List<CardEffect> effects) {
        var trigger = Triggers.youCastSpell();
        if (spellFilter != null) {
            trigger = trigger.withFilter(spellFilter);
        }
        if (stackEntryFilter != null) {
            trigger = trigger.withStackEntryFilter(stackEntryFilter);
        }
        return new TriggeredAbility(trigger, effects.toArray(CardEffect[]::new));
    }
}
