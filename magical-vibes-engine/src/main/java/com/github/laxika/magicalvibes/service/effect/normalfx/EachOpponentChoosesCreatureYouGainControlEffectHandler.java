package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureYouGainControlEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachOpponentChoosesCreatureYouGainControlEffect} (Riches). Each opponent in
 * APNAP order chooses a creature they control; after all choices, the controller gains permanent
 * control of every chosen creature simultaneously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesCreatureYouGainControlEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesCreatureYouGainControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        List<UUID> opponents = apnapOpponents(gameData, controllerId);
        beginNextOpponent(gameData, controllerId, opponents, List.of(), sourceName);
    }

    /**
     * Prompt the next opponent who still needs to choose, or seize all chosen creatures when the
     * queue is empty. Opponents with 0 creatures skip; exactly 1 auto-selects.
     */
    public void beginNextOpponent(GameData gameData, UUID gainingControllerId,
            List<UUID> remainingOpponentIds, List<UUID> accumulatedChosenIds, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        List<UUID> accumulated = new ArrayList<>(accumulatedChosenIds);

        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, opponentId, p -> true);
            if (creatureIds.isEmpty()) {
                String playerName = gameData.playerIdToName.get(opponentId);
                gameLogService.append(gameData, GameLog.text(
                        playerName + " has no creatures to choose (" + sourceName + ")."));
                log.info("Game {} - {} has no creatures for {}", gameData.id, playerName, sourceName);
                continue;
            }

            if (creatureIds.size() == 1) {
                accumulated.add(creatureIds.getFirst());
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
                if (creature != null) {
                    String playerName = gameData.playerIdToName.get(opponentId);
                    gameLogService.append(gameData, GameLog.textCardText(
                            playerName + " chooses ", creature.getCard(), " (" + sourceName + ")."));
                }
                continue;
            }

            List<UUID> rest = List.copyOf(remaining);
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.OpponentChoosesCreatureYouGainControl(
                            opponentId, gainingControllerId, sourceName, rest, List.copyOf(accumulated)));
            playerInputService.beginPermanentChoice(gameData, opponentId, creatureIds,
                    sourceName + " — choose a creature you control.");
            return;
        }

        applyControl(gameData, gainingControllerId, accumulated, sourceName);
    }

    /** Choice completion: record the pick, then prompt the next opponent or seize. */
    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.OpponentChoosesCreatureYouGainControl context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        List<UUID> accumulated = new ArrayList<>(context.accumulatedChosenIds());
        accumulated.add(permanentId);

        String playerName = gameData.playerIdToName.get(context.choosingPlayerId());
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " chooses ", chosen.getCard(), " (" + context.sourceCardName() + ")."));
        log.info("Game {} - {} chooses {} for {}", gameData.id, playerName,
                chosen.getCard().getName(), context.sourceCardName());

        beginNextOpponent(gameData, context.gainingControllerId(), context.remainingOpponentIds(),
                accumulated, context.sourceCardName());
    }

    private void applyControl(GameData gameData, UUID gainingControllerId,
            List<UUID> chosenIds, String sourceName) {
        if (chosenIds.isEmpty()) {
            return;
        }

        // Collect first — applyControlEffect moves permanents between battlefield lists.
        List<Permanent> toSeize = new ArrayList<>();
        for (UUID id : chosenIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                toSeize.add(permanent);
            }
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (Permanent permanent : toSeize) {
            creatureControlService.applyControlEffect(gameData, gainingControllerId, permanent,
                    controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, sourceName);
        }
    }

    /** Opponents of {@code controllerId} in APNAP order (active player first among them). */
    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        List<UUID> result = new ArrayList<>();
        for (UUID id : rotated) {
            if (!id.equals(controllerId)) {
                result.add(id);
            }
        }
        return result;
    }
}
