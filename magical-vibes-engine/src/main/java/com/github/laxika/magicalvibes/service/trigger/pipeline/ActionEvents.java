package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VotingResult;
import com.github.laxika.magicalvibes.model.rules.ActionEvent;

import java.util.List;
import java.util.UUID;

/**
 * The emitters of the player and keyword action events ({@link ActionEvent}): the code that takes an action calls one
 * as the action is taken, and it keeps the game's bookkeeping of the action (what was done this turn) and emits the
 * event as an occurrence of its own, which {@link ActionTriggers} matches at once. An emitter that is told of no
 * action (no player, no card sought, no die) emits nothing.
 */
public final class ActionEvents {

    private ActionEvents() {
    }

    /** {@code giverId} gave a gift. */
    public static void giftGiven(GameData gameData, UUID giverId) {
        gameData.rulesEvents.occur(new ActionEvent.GiftGiven(giverId));
    }

    /**
     * {@code schemeEntry}'s controller set it in motion (CR 701.32a). A pending "the next time you would set a scheme in
     * motion, set it in motion three times instead" replacement is used up here, and each time it is set in motion is
     * an event of its own (CR 701.32c).
     */
    public static void schemeSetInMotion(GameData gameData, StackEntry schemeEntry) {
        if (schemeEntry == null || schemeEntry.getCard() == null || schemeEntry.getControllerId() == null
                || !schemeEntry.getCard().hasType(CardType.SCHEME)) {
            return;
        }
        UUID settingPlayerId = schemeEntry.getControllerId();
        int schemesSet = 1;
        Integer pendingReplacements = gameData.pendingNextSchemeSetInMotionReplacements.get(settingPlayerId);
        if (pendingReplacements != null) {
            if (pendingReplacements <= 1) {
                gameData.pendingNextSchemeSetInMotionReplacements.remove(settingPlayerId);
            } else {
                gameData.pendingNextSchemeSetInMotionReplacements.put(settingPlayerId, pendingReplacements - 1);
            }
            schemesSet = 3;
        }
        for (int schemeIndex = 0; schemeIndex < schemesSet; schemeIndex++) {
            gameData.rulesEvents.occur(new ActionEvent.SchemeSetInMotion(schemeEntry, settingPlayerId));
        }
    }

    /**
     * {@code stackEntry}'s controller committed a crime, if the spell or ability targets an opponent, something an
     * opponent controls or a card in an opponent's graveyard (CR 700.13).
     */
    public static void crimeCommitted(GameData gameData, StackEntry stackEntry) {
        if (gameData.isCommittedCrime(stackEntry)) {
            gameData.rulesEvents.occur(new ActionEvent.CrimeCommitted(stackEntry.getControllerId()));
        }
    }

    /** The players finished a vote with {@code result}. */
    public static void votingFinished(GameData gameData, VotingResult result) {
        gameData.rulesEvents.occur(new ActionEvent.VotingFinished(result));
    }

    /** The Ring tempted {@code temptingPlayerId}, who chose {@code ringBearerId} as their Ring-bearer. */
    public static void ringBearerChosen(GameData gameData, UUID temptingPlayerId, UUID ringBearerId) {
        if (temptingPlayerId != null && ringBearerId != null) {
            gameData.rulesEvents.occur(new ActionEvent.RingBearerChosen(temptingPlayerId, ringBearerId));
        }
    }

    /** The Ring tempted {@code temptedPlayerId}. */
    public static void temptedByRing(GameData gameData, UUID temptedPlayerId) {
        gameData.rulesEvents.occur(new ActionEvent.TemptedByRing(temptedPlayerId));
    }

    /** {@code investigatingPlayerId} investigated; the first time each turn is recorded. */
    public static void investigated(GameData gameData, UUID investigatingPlayerId) {
        boolean firstThisTurn = gameData.playersWhoInvestigatedThisTurn.add(investigatingPlayerId);
        gameData.rulesEvents.occur(new ActionEvent.Investigated(investigatingPlayerId, firstThisTurn));
    }

    /** {@code rollingPlayerId} rolled the planar die. */
    public static void planarDieRolled(GameData gameData, UUID rollingPlayerId) {
        planarDieRolled(gameData, rollingPlayerId, 0);
    }

    /** {@code rollingPlayerId} rolled the planar die, its result {@code result}. */
    public static void planarDieRolled(GameData gameData, UUID rollingPlayerId, int result) {
        gameData.rulesEvents.occur(new ActionEvent.DiceRolled(rollingPlayerId, 1, result, true));
    }

    /** {@code rollingPlayerId} rolled {@code diceCount} dice at once. */
    public static void diceRolled(GameData gameData, UUID rollingPlayerId, int diceCount) {
        diceRolled(gameData, rollingPlayerId, diceCount, 0);
    }

    /** {@code rollingPlayerId} rolled {@code diceCount} dice at once, {@code result} the result of one die. */
    public static void diceRolled(GameData gameData, UUID rollingPlayerId, int diceCount, int result) {
        if (rollingPlayerId != null && diceCount > 0) {
            gameData.rulesEvents.occur(new ActionEvent.DiceRolled(rollingPlayerId, diceCount, result, false));
        }
    }

    /** {@code rollingPlayerId} rolled dice with {@code sides} faces whose natural results were {@code results}. */
    public static void dieResults(GameData gameData, UUID rollingPlayerId, int sides, int... results) {
        if (rollingPlayerId == null || sides < 1 || results == null) {
            return;
        }
        for (int result : results) {
            gameData.rulesEvents.occur(new ActionEvent.DieRolled(rollingPlayerId, sides, result));
        }
    }

    /** {@code rollingPlayerId} rolled a natural 20. */
    public static void natural20Rolled(GameData gameData, UUID rollingPlayerId) {
        if (rollingPlayerId != null) {
            gameData.rulesEvents.occur(new ActionEvent.Natural20Rolled(rollingPlayerId));
        }
    }

    /** {@code foragingPlayerId} foraged. */
    public static void foraged(GameData gameData, UUID foragingPlayerId) {
        gameData.rulesEvents.occur(new ActionEvent.Foraged(foragingPlayerId));
    }

    /** {@code seekingPlayerId} sought {@code soughtCards}; a seek that found no card is no seek of one or more. */
    public static void sought(GameData gameData, UUID seekingPlayerId, List<Card> soughtCards) {
        if (seekingPlayerId != null && soughtCards != null && !soughtCards.isEmpty()) {
            gameData.rulesEvents.occur(new ActionEvent.Sought(seekingPlayerId, soughtCards));
        }
    }

    /** {@code manifestingPlayerId} manifested dread, putting {@code cardPutIntoGraveyard} into their graveyard. */
    public static void manifestedDread(GameData gameData, UUID manifestingPlayerId, Card cardPutIntoGraveyard) {
        gameData.rulesEvents.occur(new ActionEvent.ManifestedDread(manifestingPlayerId, cardPutIntoGraveyard));
    }

    /** {@code completingPlayerId} completed a dungeon (CR 309.7). */
    public static void dungeonCompleted(GameData gameData, UUID completingPlayerId) {
        if (completingPlayerId != null) {
            gameData.rulesEvents.occur(new ActionEvent.DungeonCompleted(completingPlayerId));
        }
    }

    /** {@code discoveringPlayerId} discovered {@code discoverValue}. */
    public static void discovered(GameData gameData, UUID discoveringPlayerId, int discoverValue) {
        gameData.rulesEvents.occur(new ActionEvent.Discovered(discoveringPlayerId, discoverValue));
    }

    /** {@code conjuringPlayerId} conjured {@code cardCount} cards. */
    public static void conjured(GameData gameData, UUID conjuringPlayerId, int cardCount) {
        if (conjuringPlayerId != null && cardCount > 0) {
            gameData.rulesEvents.occur(new ActionEvent.Conjured(conjuringPlayerId, cardCount));
        }
    }

    /** {@code collectingPlayerId} collected evidence. */
    public static void evidenceCollected(GameData gameData, UUID collectingPlayerId) {
        gameData.rulesEvents.occur(new ActionEvent.EvidenceCollected(collectingPlayerId));
    }

    /** {@code fightingCreature}, controlled by {@code fightingCreatureControllerId}, fought. */
    public static void fought(GameData gameData, Permanent fightingCreature, UUID fightingCreatureControllerId) {
        if (fightingCreature != null && fightingCreatureControllerId != null) {
            gameData.rulesEvents.occur(new ActionEvent.Fought(fightingCreature, fightingCreatureControllerId));
        }
    }

    /** {@code bendingPlayerId} bent; the bending is recorded for the turn. */
    public static void bent(GameData gameData, UUID bendingPlayerId, BendingType type) {
        if (bendingPlayerId == null || type == null) {
            return;
        }
        gameData.recordBending(bendingPlayerId, type);
        gameData.rulesEvents.occur(new ActionEvent.Bent(bendingPlayerId, type));
    }

    /** {@code exertingPlayerId} exerted {@code exertedCreatureId}; the creature is recorded as exerted this turn. */
    public static void exerted(GameData gameData, UUID exertingPlayerId, UUID exertedCreatureId) {
        if (exertingPlayerId == null || exertedCreatureId == null) {
            return;
        }
        gameData.permanentsExertedThisTurn.add(exertedCreatureId);
        gameData.rulesEvents.occur(new ActionEvent.Exerted(exertingPlayerId, exertedCreatureId));
    }

    /** {@code exertingPlayerId} exerted {@code exertedCreature} as it attacked (CR 701.43d). */
    public static void exertedAsItAttacks(GameData gameData, UUID exertingPlayerId, Permanent exertedCreature) {
        if (exertingPlayerId != null && exertedCreature != null) {
            gameData.rulesEvents.occur(new ActionEvent.ExertedAsItAttacks(exertingPlayerId, exertedCreature));
        }
    }

    /** {@code monarchPlayerId} became the monarch. */
    public static void becameMonarch(GameData gameData, UUID monarchPlayerId) {
        gameData.rulesEvents.occur(new ActionEvent.BecameMonarch(monarchPlayerId));
    }

    /** {@code controllerId} solved {@code solvedCase}. */
    public static void caseSolved(GameData gameData, Permanent solvedCase, UUID controllerId) {
        gameData.rulesEvents.occur(new ActionEvent.CaseSolved(solvedCase, controllerId));
    }

    /** {@code payingPlayerId} paid {@code manaPaid} mana of a tax imposed by {@code taxingPlayerId}'s object. */
    public static void taxPaid(GameData gameData, UUID payingPlayerId, UUID taxingPlayerId, int manaPaid) {
        if (payingPlayerId != null && taxingPlayerId != null && !payingPlayerId.equals(taxingPlayerId)
                && manaPaid > 0) {
            gameData.rulesEvents.occur(new ActionEvent.TaxPaid(payingPlayerId, taxingPlayerId, manaPaid));
        }
    }

    /** A creature {@code controllerId} controls explored, revealing {@code exploredCard} ({@code null} for none). */
    public static void explored(GameData gameData, UUID controllerId, Card exploredCard) {
        gameData.rulesEvents.occur(new ActionEvent.Explored(controllerId, exploredCard));
    }

    /** {@code connivingCreature} connived, controlled by its controller now. */
    public static void connived(GameData gameData, Permanent connivingCreature) {
        if (connivingCreature != null) {
            gameData.rulesEvents.occur(new ActionEvent.Connived(connivingCreature, null));
        }
    }

    /** {@code connivingCreature} connived, controlled by {@code controllerId}, its last-known controller (CR 701.50b). */
    public static void connived(GameData gameData, Permanent connivingCreature, UUID controllerId) {
        if (connivingCreature != null && controllerId != null) {
            gameData.rulesEvents.occur(new ActionEvent.Connived(connivingCreature, controllerId));
        }
    }

    /**
     * {@code sourceCard}, the permanent {@code sourcePermanentId} controlled by {@code sourceControllerId}, exploited
     * {@code exploitedCard} (power {@code exploitedPower}), the sacrifice chosen by {@code exploitingPlayerId} with
     * {@code exploiter} the exploiting permanent as it chose.
     */
    public static void exploited(GameData gameData, Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId,
                                 UUID exploitingPlayerId, Permanent exploiter, Card exploitedCard, int exploitedPower) {
        gameData.rulesEvents.occur(new ActionEvent.Exploited(sourceCard, sourceControllerId, sourcePermanentId,
                exploitingPlayerId, exploiter, exploitedCard, exploitedPower));
    }

    /** {@code winningPlayerId} won a coin flip. */
    public static void coinFlipWon(GameData gameData, UUID winningPlayerId) {
        gameData.rulesEvents.occur(new ActionEvent.CoinFlipWon(winningPlayerId));
    }

    /** {@code losingPlayerId} lost a coin flip. */
    public static void coinFlipLost(GameData gameData, UUID losingPlayerId) {
        gameData.rulesEvents.occur(new ActionEvent.CoinFlipLost(losingPlayerId));
    }

    /** {@code foretellingPlayerId} foretold {@code foretoldCard}. */
    public static void foretold(GameData gameData, UUID foretellingPlayerId, Card foretoldCard) {
        if (foretellingPlayerId != null && foretoldCard != null) {
            gameData.rulesEvents.occur(new ActionEvent.Foretold(foretellingPlayerId, foretoldCard));
        }
    }

    /** {@code plottingPlayerId} plotted {@code plottedCard}. */
    public static void plotted(GameData gameData, UUID plottingPlayerId, Card plottedCard) {
        gameData.rulesEvents.occur(new ActionEvent.Plotted(plottingPlayerId, plottedCard));
    }

    /**
     * {@code mutatedPermanent}, controlled by {@code controllerId}, mutated: the mutation is recorded, and
     * {@code mutatedCards}' "whenever this creature mutates" abilities trigger.
     */
    public static void mutated(GameData gameData, Permanent mutatedPermanent, List<Card> mutatedCards,
                               UUID controllerId) {
        mutatedPermanent.recordMutation();
        gameData.rulesEvents.occur(new ActionEvent.Mutated(mutatedPermanent, mutatedCards, controllerId));
    }
}
