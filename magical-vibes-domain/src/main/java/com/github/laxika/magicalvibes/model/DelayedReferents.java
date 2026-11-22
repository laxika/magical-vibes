package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;

import java.util.List;
import java.util.UUID;

/**
 * What a delayed triggered ability a resolving spell or ability made refers to besides the permanent and player
 * {@link TemporaryGlobalTriggeredAbility} records ("those creatures", "that card", "the exiled cards"), and the turn it
 * waits past. The ability still affects an object it refers to that changed characteristics, and doesn't affect one
 * that left the zone it was expected in (CR 603.7c); its stack entry records them as it triggers, whatever its
 * effects are.
 *
 * @param permanentIds     the permanents it is about, recorded as its entry's declared objects
 * @param cardIds          the cards it is about, recorded as its entry's target cards; a lone one is also its
 *                         triggering card
 * @param cardEntryVersion the graveyard stay of its lone card as it was made, recorded with the triggering card so the
 *                         ability doesn't affect a card that left that graveyard and came back (CR 400.7); {@code -1}
 *                         for none
 * @param afterTurn        the turn it waits past: "at the beginning of the next turn's upkeep" made during turn N can
 *                         trigger only in a later turn; {@code -1} for none
 * @param extraTurnSequence the extra turn it is about ("at the beginning of that turn's end step" after "take an extra
 *                         turn after this one", {@code GameData.currentExtraTurnSequence}): it can trigger only in
 *                         that turn; {@code -1} for none
 * @param permanentAttachments the Auras and Equipment attached to its first permanent just before that permanent left
 *                         the battlefield (CR 608.2h), recorded by its departure and kept in its entry for "destroy
 *                         all Equipment attached to that creature" (Corrosive Ooze); {@link DepartedAttachments#NONE}
 *                         while it is still there
 * @param triggeringPermanentId the permanent its entry records as its triggering permanent when that isn't the
 *                         permanent it is about ("destroy the other creature at end of combat. ... put a +1/+1 counter
 *                         on the first creature", Infinite Authority); {@code null} when it is the permanent it is
 *                         about
 * @param combatPhase      the combat phase of the turn it is about ("at the beginning of that combat" after "there
 *                         is an additional combat phase", CR 500.8), counted as {@code GameData.combatPhasesThisTurn}
 *                         is as that phase begins: it can trigger only at that one; {@code -1} for none
 * @param stepResults      what the step it was made after did ("exile those tokens", "return those cards"), each
 *                         card bound to its stay ({@link MovedCard#entryVersion}); its stack entry reads them as that
 *                         step's outcome ({@link ResolutionResults#EMPTY} for none)
 */
public record DelayedReferents(List<UUID> permanentIds, List<UUID> cardIds, long cardEntryVersion, int afterTurn,
                               long extraTurnSequence, DepartedAttachments permanentAttachments,
                               UUID triggeringPermanentId, int combatPhase, ResolutionResults stepResults) {

    /** No objects and no turn to wait past. */
    public static final DelayedReferents NONE = new DelayedReferents(List.of(), List.of(), -1, -1, -1,
            DepartedAttachments.NONE, null, -1, ResolutionResults.EMPTY);

    public DelayedReferents {
        permanentIds = List.copyOf(permanentIds);
        cardIds = List.copyOf(cardIds);
        stepResults = stepResults == null ? ResolutionResults.EMPTY : stepResults;
        permanentAttachments = permanentAttachments == null ? DepartedAttachments.NONE : permanentAttachments;
    }

    /** About the permanents {@code permanentIds}. */
    public static DelayedReferents permanents(List<UUID> permanentIds) {
        return new DelayedReferents(permanentIds, List.of(), -1, -1, -1, DepartedAttachments.NONE, null, -1, ResolutionResults.EMPTY);
    }

    /** About the cards {@code cardIds}. */
    public static DelayedReferents cards(List<UUID> cardIds) {
        return new DelayedReferents(List.of(), cardIds, -1, -1, -1, DepartedAttachments.NONE, null, -1, ResolutionResults.EMPTY);
    }

    /** About the card {@code cardId} in the graveyard stay {@code entryVersion}. */
    public static DelayedReferents graveyardCard(UUID cardId, long entryVersion) {
        return new DelayedReferents(List.of(), List.of(
                cardId), entryVersion, -1, -1, DepartedAttachments.NONE, null, -1, ResolutionResults.EMPTY);
    }

    /** These referents, waiting past the turn {@code turnNumber}. */
    public DelayedReferents afterTurn(int turnNumber) {
        return new DelayedReferents(permanentIds, cardIds, cardEntryVersion, turnNumber, extraTurnSequence,
                permanentAttachments, triggeringPermanentId, combatPhase, stepResults);
    }

    /** These referents, about the extra turn {@code sequence} alone ("that turn's end step"). */
    public DelayedReferents duringExtraTurn(long sequence) {
        return new DelayedReferents(permanentIds, cardIds, cardEntryVersion, afterTurn, sequence,
                permanentAttachments, triggeringPermanentId, combatPhase, stepResults);
    }

    /** These referents about the permanents {@code newPermanentIds} instead. */
    public DelayedReferents withPermanentIds(List<UUID> newPermanentIds) {
        return new DelayedReferents(newPermanentIds, cardIds, cardEntryVersion, afterTurn, extraTurnSequence,
                permanentAttachments, triggeringPermanentId, combatPhase, stepResults);
    }

    /** These referents with {@code attachments} as what was attached to the first permanent as it left. */
    public DelayedReferents withPermanentAttachments(DepartedAttachments attachments) {
        return new DelayedReferents(permanentIds, cardIds, cardEntryVersion, afterTurn, extraTurnSequence,
                attachments, triggeringPermanentId, combatPhase, stepResults);
    }

    /** These referents with the permanent {@code permanentId} as the one their entry records as triggering. */
    public DelayedReferents withTriggeringPermanent(UUID permanentId) {
        return new DelayedReferents(permanentIds, cardIds, cardEntryVersion, afterTurn, extraTurnSequence,
                permanentAttachments, permanentId, combatPhase, stepResults);
    }

    /** These referents, about the combat phase {@code phase} of the turn alone ("at the beginning of that combat"). */
    public DelayedReferents duringCombatPhase(int phase) {
        return new DelayedReferents(permanentIds, cardIds, cardEntryVersion, afterTurn, extraTurnSequence,
                permanentAttachments, triggeringPermanentId, phase, stepResults);
    }

    /** These referents, with {@code results} as what the step the ability was made after did. */
    public DelayedReferents withStepResults(ResolutionResults results) {
        return new DelayedReferents(permanentIds, cardIds, cardEntryVersion, afterTurn, extraTurnSequence,
                permanentAttachments, triggeringPermanentId, combatPhase, results);
    }
}
