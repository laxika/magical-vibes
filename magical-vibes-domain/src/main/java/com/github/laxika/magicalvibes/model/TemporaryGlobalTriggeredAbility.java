package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ControlChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;

import java.util.List;
import java.util.UUID;

/**
 * A global triggered ability registered by a resolving effect.
 * The source card is retained because the spell that registered the ability is no longer on the
 * battlefield when the trigger fires. Duration flags distinguish ordinary end-of-turn triggers,
 * end-of-next-turn triggers, and triggers that expire at the beginning of the controller's next
 * turn.
 * The optional watched permanent identifies a particular object for a delayed event trigger.
 *
 * @param ability            the ability: every effect of it goes on the stack as one object (CR 603.3)
 * @param sourcePermanentId  the permanent that was the source of the ability that registered this one, which is this
 *                           one's source too (CR 603.7e), recorded whatever the effects are: the "this" of "whenever you
 *                           cast a spell or play a land from among cards exiled with this" (Apple of Eden, Isu Relic),
 *                           judged by the trigger's {@code exiledWithThis()} filter; {@code null} when it had none
 * @param watchedPlayerId    the player the registering ability named (its target, or the defending player of an attack
 *                           trigger), recorded whatever the effects are: the "they" of "until the end of defending
 *                           player's next turn, that player gets two rad counters whenever they cast a spell" (Nuka-Nuke
 *                           Launcher), judged by the trigger's {@code StackEntryControlledByWatchedPlayerPredicate}
 *                           filter; {@code null} when it named none
 * @param linkedExiledCardId the card the registering ability's source had exiled with its linked ability as the
 *                           ability registered (CR 603.7c), recorded whatever the effects are: Surge to Victory's "the
 *                           exiled card", which the collector puts on the entry as its linked exiled card, so casting
 *                           the same spell again and exiling another card can't redirect it; {@code null} when it had
 *                           exiled none
 * @param untilTriggered     whether it has no stated duration: it triggers only the next time its trigger event occurs
 *                           and lasts until then (CR 603.7b), so no turn's end removes it; "when that creature leaves
 *                           the battlefield or becomes untapped" (Merieke Ri Berit). One about a permanent that left the
 *                           battlefield without triggering it never can: the permanent is gone (CR 400.7)
 * @param next               whether it triggers only the next time its trigger event occurs within its stated
 *                           duration: "when you next cast an instant or sorcery spell this turn, copy that spell"
 *                           triggers once and ends with the turn if it hasn't (CR 603.7b)
 * @param boon               whether it is a one-time boon ("you get a one-time boon with 'when you cast a creature spell,
 *                           ...'"): it has no stated duration and triggers once, as {@code untilTriggered}, and it is
 *                           what "if you have a boon" counts (Underbridge Warlock)
 * @param referents          the other objects it refers to and the turn it waits past ({@link DelayedReferents#NONE}
 *                           for none)
 * @param repeats            whether its stated duration lets it trigger every time its trigger event occurs, however
 *                           many turns that takes: "for the rest of the game", "for as long as ...", which a turn's end
 *                           doesn't remove (CR 603.7b)
 * @param lastsWhile         the condition its "for as long as ..." duration holds while, judged as its trigger event
 *                           occurs: once it is false the ability has ended and is removed without triggering; {@code
 *                           null} for a duration with no such condition. Read for step abilities
 * @param payToPrevent       the mana cost the player or planeswalker's controller it is about may pay, as a special
 *                           action, to stop it from triggering ("unless that player pays {2} before that step",
 *                           CR 116.2c); {@code null} when nothing stops it
 * @param uses               how many times it triggers before it ends, when it triggers only a limited number of
 *                           times: a "two-time boon" triggers twice and ends as it triggers the second time; 1 for any
 *                           other
 */
public record TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                              TargetFilter targetFilter,
                                              boolean untilEndOfNextTurn, boolean untilNextTurn,
                                              int registrationTurnNumber,
                                              UUID expirationPlayerId,
                                              UUID watchedPermanentId,
                                              UUID sourcePermanentId,
                                              UUID watchedPlayerId,
                                              UUID linkedExiledCardId,
                                              boolean untilTriggered,
                                              boolean next,
                                              boolean boon,
                                              DelayedReferents referents,
                                              String payToPrevent,
                                              boolean repeats,
                                              Condition lastsWhile,
                                              int uses) {

    public TemporaryGlobalTriggeredAbility {
        if (referents == null) {
            referents = DelayedReferents.NONE;
        }
        if (uses < 1) {
            throw new IllegalArgumentException("A delayed triggered ability triggers at least once");
        }
    }

    /** {@code ability} that triggers once, or for its whole duration. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber,
                                           UUID expirationPlayerId,
                                           UUID watchedPermanentId,
                                           UUID sourcePermanentId,
                                           UUID watchedPlayerId,
                                           UUID linkedExiledCardId,
                                           boolean untilTriggered,
                                           boolean next,
                                           boolean boon,
                                           DelayedReferents referents,
                                           String payToPrevent,
                                           boolean repeats,
                                           Condition lastsWhile) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, untilTriggered, next, boon, referents, payToPrevent, repeats, lastsWhile, 1);
    }

    /** {@code ability} that doesn't repeat. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber,
                                           UUID expirationPlayerId,
                                           UUID watchedPermanentId,
                                           UUID sourcePermanentId,
                                           UUID watchedPlayerId,
                                           UUID linkedExiledCardId,
                                           boolean untilTriggered,
                                           boolean next,
                                           boolean boon,
                                           DelayedReferents referents,
                                           String payToPrevent) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, untilTriggered, next, boon, referents, payToPrevent, false, null);
    }

    /** {@code ability} that nothing stops by paying for it. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber,
                                           UUID expirationPlayerId,
                                           UUID watchedPermanentId,
                                           UUID sourcePermanentId,
                                           UUID watchedPlayerId,
                                           UUID linkedExiledCardId,
                                           boolean untilTriggered,
                                           boolean next,
                                           boolean boon,
                                           DelayedReferents referents) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, untilTriggered, next, boon, referents, null);
    }

    /** {@code ability} that refers to nothing more than its permanent and player. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber, UUID expirationPlayerId,
                                           UUID watchedPermanentId, UUID sourcePermanentId, UUID watchedPlayerId,
                                           UUID linkedExiledCardId, boolean untilTriggered, boolean next,
                                           boolean boon) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, untilTriggered, next, boon, DelayedReferents.NONE);
    }

    /** {@code ability} that isn't a boon. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber, UUID expirationPlayerId,
                                           UUID watchedPermanentId, UUID sourcePermanentId, UUID watchedPlayerId,
                                           UUID linkedExiledCardId, boolean untilTriggered, boolean next) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, untilTriggered, next, false);
    }

    /** {@code ability} that triggers on every event of its duration, or with no stated duration (CR 603.7b). */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber, UUID expirationPlayerId,
                                           UUID watchedPermanentId, UUID sourcePermanentId, UUID watchedPlayerId,
                                           UUID linkedExiledCardId, boolean untilTriggered) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, untilTriggered, false);
    }

    /** {@code ability} with a stated duration (CR 603.7b). */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber, UUID expirationPlayerId,
                                           UUID watchedPermanentId, UUID sourcePermanentId, UUID watchedPlayerId,
                                           UUID linkedExiledCardId) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, false);
    }

    /**
     * {@code ability} of the delayed triggered ability a resolving spell or ability with the source {@code sourceCard}
     * and {@code sourcePermanentId} (or none) created, controlled by {@code controllerId}, about the permanent
     * {@code watchedPermanentId} ("when that creature leaves the battlefield this turn"): until end of turn when
     * {@code thisTurn}, else until it triggers (CR 603.7b).
     */
    public static TemporaryGlobalTriggeredAbility about(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                                        UUID watchedPermanentId, UUID sourcePermanentId,
                                                        boolean thisTurn) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                watchedPermanentId, sourcePermanentId, null, null, !thisTurn);
    }

    /**
     * {@code ability} of the delayed triggered ability a resolving spell or ability with the source {@code sourceCard}
     * and {@code sourcePermanentId} (or none) created until end of turn, controlled by {@code controllerId}, that
     * watches no particular object ("whenever a creature dealt damage this way dies this turn").
     */
    public static TemporaryGlobalTriggeredAbility thisTurn(UUID controllerId, Card sourceCard,
                                                           TriggeredAbility ability, UUID sourcePermanentId) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                null, sourcePermanentId, null, null, false);
    }

    /**
     * {@code ability} of the delayed triggered ability "when you next [event] this turn, ..." a resolving spell or
     * ability (or a mana ability) with the source {@code sourceCard} and {@code sourcePermanentId} (or none) created,
     * controlled by {@code controllerId}: it triggers only the next time its event occurs this turn (CR 603.7b), and
     * ends with the turn if it hasn't.
     */
    public static TemporaryGlobalTriggeredAbility nextThisTurn(UUID controllerId, Card sourceCard,
                                                               TriggeredAbility ability, UUID sourcePermanentId) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                null, sourcePermanentId, null, null, false, true);
    }

    /**
     * {@code ability} of the delayed triggered ability "until your next turn, whenever ..." a resolving spell or ability
     * with the source {@code sourceCard} and {@code sourcePermanentId} (or none) created on turn
     * {@code registrationTurnNumber}, controlled by {@code controllerId}: it ends as that player's next turn begins.
     */
    public static TemporaryGlobalTriggeredAbility untilYourNextTurn(UUID controllerId, Card sourceCard,
                                                                    TriggeredAbility ability, UUID sourcePermanentId,
                                                                    int registrationTurnNumber) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, true,
                registrationTurnNumber, null, null, sourcePermanentId, null, null, false, false);
    }

    /**
     * {@code ability} of the one-time boon {@code controllerId} gets from a resolving spell or ability with the source
     * {@code sourceCard} and {@code sourcePermanentId} (or none): it lasts until it triggers, once.
     */
    public static TemporaryGlobalTriggeredAbility boon(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                                       UUID sourcePermanentId) {
        return boon(controllerId, sourceCard, ability, sourcePermanentId, 1);
    }

    /**
     * {@code ability} of the {@code uses}-time boon {@code controllerId} gets from a resolving spell or ability with the
     * source {@code sourceCard} and {@code sourcePermanentId} (or none): "you get a two-time boon with 'whenever you
     * sacrifice one or more permanents, ...'" lasts until it has triggered {@code uses} times.
     */
    public static TemporaryGlobalTriggeredAbility boon(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                                       UUID sourcePermanentId, int uses) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                null, sourcePermanentId, null, null, true, false, true, DelayedReferents.NONE, null, false, null,
                uses);
    }

    /**
     * {@code ability} of the boon {@code controllerId} gets from a resolving spell or ability with the source
     * {@code sourceCard} and {@code sourcePermanentId} (or none) that its own ability uses up as it resolves: "you get a
     * one-time boon with 'at the beginning of your end step, if ..., ...'" (Underbridge Warlock) triggers at each of its
     * steps whose "if" holds until its ability has resolved, and is a boon its controller has until then.
     */
    public static TemporaryGlobalTriggeredAbility boonUsedAsItResolves(UUID controllerId, Card sourceCard,
                                                                       TriggeredAbility ability,
                                                                       UUID sourcePermanentId) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                null, sourcePermanentId, null, null, false, false, true, DelayedReferents.NONE, null, true, null);
    }

    /** This delayed ability, its targets, chosen as it goes on the stack (CR 603.3d), restricted to {@code filter}. */
    public TemporaryGlobalTriggeredAbility withTargetFilter(TargetFilter filter) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, filter,
                untilEndOfNextTurn, untilNextTurn, registrationTurnNumber, expirationPlayerId, watchedPermanentId,
                sourcePermanentId, watchedPlayerId, linkedExiledCardId, untilTriggered, next, boon, referents,
                payToPrevent, repeats, lastsWhile, uses);
    }

    /** This delayed ability with {@code remaining} uses left. */
    public TemporaryGlobalTriggeredAbility withUses(int remaining) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, targetFilter,
                untilEndOfNextTurn, untilNextTurn, registrationTurnNumber, expirationPlayerId, watchedPermanentId,
                sourcePermanentId, watchedPlayerId, linkedExiledCardId, untilTriggered, next, boon, referents,
                payToPrevent, repeats, lastsWhile, remaining);
    }

    /**
     * {@code ability}, whose trigger is the beginning of a step or phase ("at the beginning of the next end step",
     * "at the beginning of your next upkeep"), of the delayed triggered ability a resolving spell or ability with the
     * source {@code sourceCard} and {@code sourcePermanentId} (or none) created, controlled by {@code controllerId}. It
     * has no stated duration, so it triggers only the next time its step begins (CR 603.7b), however its objects fared
     * (CR 603.7c). It is about the permanent {@code permanentId} and the player {@code playerId} (or neither), and
     * {@code referents}.
     */
    public static TemporaryGlobalTriggeredAbility atNextStep(UUID controllerId, Card sourceCard,
                                                             TriggeredAbility ability, UUID sourcePermanentId,
                                                             UUID permanentId, UUID playerId,
                                                             DelayedReferents referents) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                permanentId, sourcePermanentId, playerId, null, true, false, false, referents);
    }

    /**
     * {@link #atNextStep(UUID, Card, TriggeredAbility, UUID, UUID, UUID, DelayedReferents)} with the trigger
     * {@code step} and the effects {@code effects}, about the permanent {@code permanentId} alone (or nothing).
     */
    public static TemporaryGlobalTriggeredAbility atNextStep(StepTrigger step, UUID controllerId, Card sourceCard,
                                                             UUID sourcePermanentId, UUID permanentId,
                                                             CardEffect... effects) {
        return atNextStep(controllerId, sourceCard, new TriggeredAbility(step, effects), sourcePermanentId,
                permanentId, null, permanentId == null ? DelayedReferents.NONE
                        : DelayedReferents.permanents(List.of(permanentId)));
    }

    /**
     * {@link #atNextStep(UUID, Card, TriggeredAbility, UUID, UUID, UUID, DelayedReferents)} that the player or
     * planeswalker's controller it is about can stop by paying {@code manaCost} before the step, as a special action
     * (CR 116.2c).
     */
    public static TemporaryGlobalTriggeredAbility atNextStepUnlessPaid(UUID controllerId, Card sourceCard,
                                                                       TriggeredAbility ability,
                                                                       UUID sourcePermanentId, UUID permanentId,
                                                                       UUID playerId, String manaCost) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                permanentId, sourcePermanentId, playerId, null, true, false, false, DelayedReferents.NONE, manaCost);
    }

    /**
     * {@link #atNextStep(StepTrigger, UUID, Card, UUID, UUID, CardEffect...)} with a stated duration: "at this turn's
     * next end of combat" triggers only the next time its step begins this turn and ends with the turn if it hasn't
     * (CR 603.7b). It is about the objects {@code referents} and the permanent {@code permanentId} (or none).
     */
    public static TemporaryGlobalTriggeredAbility atNextStepThisTurn(StepTrigger step, UUID controllerId,
                                                                     Card sourceCard, UUID sourcePermanentId,
                                                                     UUID permanentId, DelayedReferents referents,
                                                                     CardEffect... effects) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, new TriggeredAbility(step, effects), null,
                false, false, -1, null, permanentId, sourcePermanentId, null, null, false, true, false, referents);
    }

    /**
     * {@code ability}, whose trigger is the beginning of a step or phase, of the delayed triggered ability a resolving
     * spell or ability with the source {@code sourceCard} and {@code sourcePermanentId} (or none) created, controlled
     * by {@code controllerId}, with a stated duration that lets it trigger every time its step begins: "at the
     * beginning of each of your upkeeps for the rest of the game", or, with {@code lastsWhile}, "for as long as ...
     * at the beginning of each upkeep" (CR 603.7b). It ends, without triggering, as its step begins while
     * {@code lastsWhile} is false. It is about the permanent {@code permanentId} (or none) and {@code referents}.
     */
    public static TemporaryGlobalTriggeredAbility atEveryStep(UUID controllerId, Card sourceCard,
                                                              TriggeredAbility ability, UUID sourcePermanentId,
                                                              UUID permanentId, DelayedReferents referents,
                                                              Condition lastsWhile) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                permanentId, sourcePermanentId, null, null, false, false, false, referents, null, true, lastsWhile);
    }

    /**
     * {@code ability} of the delayed triggered ability that a player controlling {@code controllerId} created by taking
     * the action a static ability of {@code sourceCard} allows, its source (CR 603.7g), that triggers every time its
     * event occurs for the rest of the game: "you may reveal this card from your opening hand. If you do, when each
     * opponent casts their first spell of the game, ..." (Chancellor of the Annex).
     */
    public static TemporaryGlobalTriggeredAbility forTheRestOfTheGame(UUID controllerId, Card sourceCard,
                                                                      TriggeredAbility ability) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                null, null, null, null, false, false, false, DelayedReferents.NONE, null, true, null);
    }

    /** This delayed ability with the ability {@code newAbility}. */
    public TemporaryGlobalTriggeredAbility withAbility(TriggeredAbility newAbility) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, newAbility, targetFilter,
                untilEndOfNextTurn, untilNextTurn, registrationTurnNumber, expirationPlayerId, watchedPermanentId,
                sourcePermanentId, watchedPlayerId, linkedExiledCardId, untilTriggered, next, boon, referents,
                payToPrevent, repeats, lastsWhile, uses);
    }

    /**
     * This ability about the player {@code playerId}: "at the beginning of each of that player's upkeeps"
     * ({@code ActivePlayer.CHOSEN_PLAYER}).
     */
    public TemporaryGlobalTriggeredAbility withWatchedPlayer(UUID playerId) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn,
                untilNextTurn, registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId,
                playerId, linkedExiledCardId, untilTriggered, next, boon, referents, payToPrevent, repeats,
                lastsWhile, uses);
    }

    /** This ability with the other objects it refers to and the turn it waits past {@code newReferents}. */
    public TemporaryGlobalTriggeredAbility withReferents(DelayedReferents newReferents) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn,
                untilNextTurn, registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId,
                watchedPlayerId, linkedExiledCardId, untilTriggered, next, boon, newReferents, payToPrevent, repeats,
                lastsWhile, uses);
    }

    /**
     * This ability, keeping the number the resolution that made it noted ({@code NoteAmountEffect}) as its
     * {@link StepKind#NOTE} step result, which its entry reads as it resolves ("where X is the noted number"); unchanged
     * when {@code made}, that resolution's results, noted none. The number is the one noted as the maker resolved
     * (CR 603.7a, 608.2h).
     */
    public TemporaryGlobalTriggeredAbility keepingNotedAmount(ResolutionResults made) {
        if (!made.outcomes().containsKey(StepKind.NOTE)) {
            return this;
        }
        return withReferents(referents.withStepResults(
                referents.stepResults().withOutcome(StepKind.NOTE, made.outcome(StepKind.NOTE))));
    }

    /** Whether its trigger is the beginning of a step or phase, which it waits for whatever its objects do. */
    public boolean waitsForStep() {
        return ability.conditions().stream().anyMatch(condition -> condition.trigger() instanceof StepTrigger);
    }

    /**
     * Whether its trigger is its controller losing control of the permanent it is about ("when you lose control of that
     * Equipment this turn"): the cleanup step keeps such an ability until control is recomputed, so it triggers as the
     * control effect that ends with it ends (CR 514.2).
     */
    public boolean watchesControlLoss() {
        return ability.conditions().stream().anyMatch(condition -> condition.trigger() instanceof ControlChangeTrigger
                control && control.event() == ControlChangeTrigger.ControlEvent.YOU_LOSE);
    }

    /**
     * Whether it triggers only a limited number of times, {@link #uses}: it has no stated duration, or it is a "when you
     * next ..." (CR 603.7b).
     */
    public boolean triggersOnce() {
        return untilTriggered || next;
    }

    /** Whether a turn's end removes it: it has a stated duration that ends then (CR 603.7b, 514.2). */
    public boolean endsThisTurn() {
        return !untilTriggered && !repeats && !untilEndOfNextTurn && !untilNextTurn;
    }

    /** {@code ability} with no linked exiled card. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter, boolean untilEndOfNextTurn,
                                           boolean untilNextTurn, int registrationTurnNumber,
                                           UUID expirationPlayerId, UUID watchedPermanentId,
                                           UUID sourcePermanentId, UUID watchedPlayerId) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                null);
    }

    /** {@code ability} with no registering source permanent and no watched player. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter, boolean untilEndOfNextTurn,
                                           boolean untilNextTurn, int registrationTurnNumber,
                                           UUID expirationPlayerId, UUID watchedPermanentId) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, null, null);
    }

    /** {@code ability} until end of turn, with no target filter and no watched permanent. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability) {
        this(controllerId, sourceCard, ability, null, false, false, -1, null, null);
    }
}
