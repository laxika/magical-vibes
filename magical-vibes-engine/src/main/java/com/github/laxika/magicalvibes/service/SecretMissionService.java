package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SecretMissionEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Secret Mission conspiracies: "During your end step, if you meet the condition, you may reveal your
 * choice and turn this card face down. When you do, collect the reward." Revealing is a special action
 * the owner takes while holding priority in their own end step; collecting the reward is a reflexive
 * triggered ability.
 */
@Slf4j
@Service
public class SecretMissionService {

    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;

    // The game log service sits upstream of the view projection that asks this service for revealable missions.
    public SecretMissionService(ConditionEvaluationService conditionEvaluationService,
                                @Lazy GameLogService gameLogService) {
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
    }

    /** The Secret Mission cards in the player's command zone they could reveal right now. */
    public List<UUID> revealableMissionCardIds(GameData gameData, UUID playerId) {
        List<UUID> revealable = new ArrayList<>();
        if (!playerId.equals(gameData.activePlayerId) || gameData.currentStep != TurnStep.END_STEP) {
            return revealable;
        }
        for (Card card : gameData.playerCommandZones.getOrDefault(playerId, List.of())) {
            if (chosenMissionMet(gameData, playerId, card)) {
                revealable.add(card.getId());
            }
        }
        return revealable;
    }

    /**
     * Reveals the chosen mission of the Secret Mission card {@code cardId}, turns it face down and puts
     * its reward on the stack. Priority checks are the caller's.
     */
    public void reveal(GameData gameData, UUID playerId, UUID cardId) {
        if (!playerId.equals(gameData.activePlayerId) || gameData.currentStep != TurnStep.END_STEP) {
            throw new IllegalStateException("A secret mission can be revealed only during your end step");
        }
        Card card = gameData.playerCommandZones.getOrDefault(playerId, List.of()).stream()
                .filter(candidate -> candidate.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("That card is not in your command zone"));
        if (!chosenMissionMet(gameData, playerId, card)) {
            throw new IllegalStateException("Your chosen mission's condition is not met");
        }
        SecretMissionEffect secretMission = secretMission(card);
        SecretMissionEffect.Mission mission =
                secretMission.missions().get(gameData.secretMissionChoices.get(card.getId()));

        gameData.faceDownCommandZoneCards.add(card.getId());
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " reveals the mission \""
                + mission.label() + "\" and turns ", card, " face down."));

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                playerId,
                card.getName() + "'s reward",
                new ArrayList<>(List.of(secretMission.reward()))));
        gameData.priorityPassedBy.clear();
        log.info("Game {} - {} reveals the secret mission of {}", gameData.id, playerName, card.getName());
    }

    private boolean chosenMissionMet(GameData gameData, UUID playerId, Card card) {
        SecretMissionEffect secretMission = secretMission(card);
        Integer missionIndex = gameData.secretMissionChoices.get(card.getId());
        if (secretMission == null || missionIndex == null
                || gameData.faceDownCommandZoneCards.contains(card.getId())) {
            return false;
        }
        return conditionEvaluationService.isMet(gameData,
                secretMission.missions().get(missionIndex).condition(), ConditionContext.forCard(card, playerId));
    }

    private static SecretMissionEffect secretMission(Card card) {
        return card.getEffects(EffectSlot.COMMAND_ZONE_STATIC).stream()
                .filter(SecretMissionEffect.class::isInstance)
                .map(SecretMissionEffect.class::cast)
                .findFirst()
                .orElse(null);
    }
}
