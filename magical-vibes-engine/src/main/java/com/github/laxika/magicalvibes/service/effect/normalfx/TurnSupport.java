package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedAdditionalCombatBeginningEffect;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentAtEndOfCombatThenPutCounterOnSource;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.ExileAndReturnTransformedAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PhaseOutAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.TapAndSkipUntapAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.TapCombatOpponentsAtEndOfCombat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared turn helpers used by every "normal" Turn effect handler.
 *
 * <p>Extracted verbatim from {@code TurnResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurnSupport {

    private final CombatService combatService;
    private final GameLogService gameLogService;
    private final CreatureControlService creatureControlService;
    private final TurnCleanupService turnCleanupService;
    private final ExileService exileService;

    @Autowired
    @Lazy
    private PlayerInputService playerInputService;

    public UUID resolveTargetPlayer(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return null;
        }
        return targetPlayerId;
    }

    public void exileStackEntries(GameData gameData) {
        exileStackEntries(gameData, "end the turn");
    }

    public void exileStackEntries(GameData gameData, String reason) {
        // The resolving spell (e.g. Time Stop) is already removed from the stack by resolveTopOfStack,
        // so we only need to handle remaining entries.
        List<StackEntry> remaining = new ArrayList<>(gameData.stack);
        gameData.stack.clear();

        Set<StackEntryType> spellTypes = Set.of(
                StackEntryType.CREATURE_SPELL, StackEntryType.INSTANT_SPELL,
                StackEntryType.SORCERY_SPELL, StackEntryType.ENCHANTMENT_SPELL,
                StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL,
                StackEntryType.BATTLE_SPELL
        );

        for (StackEntry se : remaining) {
            if (spellTypes.contains(se.getEntryType()) && !se.isCopy()) {
                Card card = se.getPhysicalCard();
                exileService.exileCard(gameData, se.getOwnerId(), card);
                gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
                log.info("Game {} - {} exiled from stack ({})", gameData.id, card.getName(), reason);
            }
            // Triggered/activated abilities just cease to exist
        }
    }

    public void clearCombatState(GameData gameData) {
        gameData.expireEndOfCombatFloatingEffects();
        combatService.clearCombatState(gameData);
        gameData.clearDelayedActions(SacrificeAtEndOfCombat.class);
        gameData.clearDelayedActions(DelayedEndOfCombatTrigger.class);
        gameData.clearDelayedActions(TapAndSkipUntapAtEndOfCombat.class);
        gameData.clearDelayedActions(TapCombatOpponentsAtEndOfCombat.class);
        gameData.clearDelayedActions(PhaseOutAtEndOfCombat.class);
        gameData.clearDelayedActions(DealDamageToPermanentAtEndOfCombat.class);
        gameData.clearDelayedActions(DestroyCombatOpponentsAtEndOfCombat.class);
        gameData.clearDelayedActions(DestroyEquipmentAtEndOfCombat.class);
        gameData.clearDelayedActions(PutMinusOneCounterAtEndOfCombat.class);
        gameData.clearDelayedActions(PutCounterOnPermanentAtEndOfCombat.class);
        gameData.clearDelayedActions(DestroyCombatOpponentAtEndOfCombatThenPutCounterOnSource.class);
        gameData.clearDelayedActions(RemoveCounterFromSourceAtEndOfCombat.class);
        gameData.clearDelayedActions(GainControlOfPermanentAtEndOfCombat.class);
        gameData.clearDelayedActions(ExileAndReturnTransformedAtEndOfCombat.class);
        gameData.clearDelayedActions(DelayedPermanentAction.class,
                a -> a.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT
                        || a.kind() == DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT
                        || a.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_OF_COMBAT
                        || a.kind() == DelayedPermanentActionKind.PUT_ON_TOP_OF_LIBRARY_AT_END_OF_COMBAT);
    }

    public void endCombatPhase(GameData gameData) {
        clearCombatState(gameData);
        gameData.creaturesWithCombatDamagePreventedThisCombat.clear();
        gameData.creaturesPreventedFromDealingCombatDamageThisCombat.clear();
        gameData.onlyLandCreaturesCanAttackThisCombat = false;
        gameData.onlyAggressiveCreaturesCanAttackThisCombat = false;
        gameData.creaturesCantAttackThisCombat = false;
        gameData.onlyPermanentCanAttackThisCombatId = null;
        gameData.onlyPermanentsCanAttackThisCombatIds = null;
        gameData.clearDelayedActions(DelayedAdditionalCombatBeginningEffect.class);
        gameData.additionalCombatPhasesOnly = 0;
        gameData.aggressiveCombatPhasesOnly = 0;
        if (gameData.mindControlUntilEndOfCombat) {
            gameData.mindControlledPlayerId = null;
            gameData.mindControllerPlayerId = null;
            gameData.mindControlUntilEndOfCombat = false;
        }
        if (gameData.additionalCombatReturnActivePlayerId != null) {
            gameData.activePlayerId = gameData.additionalCombatReturnActivePlayerId;
            gameData.additionalCombatReturnActivePlayerId = null;
        }
        turnCleanupService.drainManaPools(gameData);
        gameData.revertableManaActivations.clear();
        gameData.priorityPassedBy.clear();
        gameData.interaction.clearAwaitingInput();
        gameData.currentStep = TurnStep.POSTCOMBAT_MAIN;
    }

    public void skipToCleanupStep(GameData gameData) {
        if (gameData.currentExtraTurnSequence != null) {
            gameData.drainDelayedActions(LoseGameAtEndStep.class,
                    action -> gameData.currentExtraTurnSequence.equals(action.extraTurnSequence()));
        }
        gameData.currentStep = TurnStep.CLEANUP;
        creatureControlService.reconcileControl(gameData);
        gameData.controlLossUnattachTriggers.clear();
        gameData.priorityPassedBy.clear();

        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null) {
            turnCleanupService.applyCleanupResets(gameData);
            return;
        }
        List<Card> hand = gameData.playerHands.get(activePlayerId);
        int maxHandSize = Math.max(turnCleanupService.getMaxHandSize(gameData, activePlayerId), 0);
        if (hand != null && hand.size() > maxHandSize
                && !turnCleanupService.hasNoMaximumHandSize(gameData, activePlayerId)) {
            gameData.cleanupDiscardPending = true;
            gameData.discardCausedByOpponent = false;
            playerInputService.beginDiscardChoice(gameData, activePlayerId, hand.size() - maxHandSize);
        } else {
            turnCleanupService.applyCleanupResets(gameData);
        }
    }

    public static String pluralize(String word, int count) {
        return count == 1 ? word : word + "s";
    }
}
