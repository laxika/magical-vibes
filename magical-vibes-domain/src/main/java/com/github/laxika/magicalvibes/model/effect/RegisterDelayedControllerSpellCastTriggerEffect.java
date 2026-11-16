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
 * @param duration         how long it lasts and how often it triggers (CR 603.7b)
 * @param targetFilter     what its targets, chosen as it goes on the stack (CR 603.3d), must match; {@code null} = the
 *                         effects' own
 */
public record RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                               StackEntryPredicate stackEntryFilter,
                                                               List<CardEffect> resolvedEffects,
                                                               Duration duration,
                                                               TargetFilter targetFilter)
        implements CardEffect {

    /** How long a delayed spell-cast ability lasts and how often it triggers (CR 603.7b). */
    public enum Duration {
        /** "Until end of turn, whenever you cast ...": every matching spell this turn. */
        THIS_TURN,
        /** "When you next cast ... this turn": the next matching spell this turn only. */
        NEXT_THIS_TURN,
        /** "Until your next turn, whenever you cast ...": every matching spell until its controller's next turn. */
        UNTIL_YOUR_NEXT_TURN,
        /** A one-time boon: the next matching spell, however many turns later. */
        BOON;

        /**
         * The delayed ability {@code ability} of {@code controllerId} this duration makes, from a resolution of an
         * object with the source {@code sourceCard} and {@code sourcePermanentId} (or none) on turn
         * {@code turnNumber}, whose targets must match {@code targetFilter} (or the effects' own when {@code null}).
         */
        public TemporaryGlobalTriggeredAbility delayed(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                                       UUID sourcePermanentId, int turnNumber,
                                                       TargetFilter targetFilter) {
            TemporaryGlobalTriggeredAbility delayed = switch (this) {
                case THIS_TURN -> TemporaryGlobalTriggeredAbility.thisTurn(controllerId, sourceCard, ability,
                        sourcePermanentId);
                case NEXT_THIS_TURN -> TemporaryGlobalTriggeredAbility.nextThisTurn(controllerId, sourceCard, ability,
                        sourcePermanentId);
                case UNTIL_YOUR_NEXT_TURN -> TemporaryGlobalTriggeredAbility.untilYourNextTurn(controllerId,
                        sourceCard, ability, sourcePermanentId, turnNumber);
                case BOON -> TemporaryGlobalTriggeredAbility.boon(controllerId, sourceCard, ability,
                        sourcePermanentId);
            };
            return targetFilter == null ? delayed : new TemporaryGlobalTriggeredAbility(delayed.controllerId(),
                    delayed.sourceCard(), delayed.ability(), targetFilter, delayed.untilEndOfNextTurn(),
                    delayed.untilNextTurn(), delayed.registrationTurnNumber(), delayed.expirationPlayerId(),
                    delayed.watchedPermanentId(), delayed.sourcePermanentId(), delayed.watchedPlayerId(),
                    delayed.linkedExiledCardId(), delayed.untilTriggered(), delayed.next(), delayed.boon());
        }
    }

    /** "Until end of turn, whenever you cast a [spellFilter] spell, [effects]." */
    public static RegisterDelayedControllerSpellCastTriggerEffect thisTurn(CardPredicate spellFilter,
                                                                          List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.THIS_TURN, null);
    }

    /** "When you next cast a [spellFilter] spell this turn, [effects]." */
    public static RegisterDelayedControllerSpellCastTriggerEffect nextThisTurn(CardPredicate spellFilter,
                                                                              List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.NEXT_THIS_TURN, null);
    }

    /** "Until your next turn, whenever you cast a [spellFilter] spell, [effects]." */
    public static RegisterDelayedControllerSpellCastTriggerEffect untilYourNextTurn(CardPredicate spellFilter,
                                                                                   List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.UNTIL_YOUR_NEXT_TURN, null);
    }

    /** "You get a one-time boon with 'When you cast a [spellFilter] spell, [effects].'" */
    public static RegisterDelayedControllerSpellCastTriggerEffect boon(CardPredicate spellFilter,
                                                                      List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, null, resolvedEffects,
                Duration.BOON, null);
    }

    /** This registration, triggering only for a spell whose stack object matches {@code filter} too. */
    public RegisterDelayedControllerSpellCastTriggerEffect withStackEntryFilter(StackEntryPredicate filter) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, filter, resolvedEffects, duration,
                targetFilter);
    }

    /** This registration, its ability's targets restricted to {@code filter}. */
    public RegisterDelayedControllerSpellCastTriggerEffect withTargetFilter(TargetFilter filter) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(spellFilter, stackEntryFilter, resolvedEffects,
                duration, filter);
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
