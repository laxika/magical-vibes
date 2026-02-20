package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggeredAbilityQueueService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    public void processNextDeathTriggerTarget(GameData gameData) {
        while (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            PermanentChoiceContext.DeathTriggerTarget pending = gameData.pendingDeathTriggerTargets.peekFirst();

            // Collect valid creature targets from all battlefields
            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }

            if (validTargets.isEmpty()) {
                // No valid targets â€” trigger can't go on the stack, skip it
                gameData.pendingDeathTriggerTargets.removeFirst();
                String logEntry = pending.dyingCard().getName() + "'s death trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} death trigger skipped (no valid creature targets)",
                        gameData.id, pending.dyingCard().getName());
                continue;
            }

            // Remove from queue and begin permanent choice
            gameData.pendingDeathTriggerTargets.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.dyingCard().getName() + "'s ability â€” Choose target creature.");

            String logEntry = pending.dyingCard().getName() + "'s death trigger â€” choose a target creature.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} death trigger awaiting target selection", gameData.id, pending.dyingCard().getName());
            return;
        }
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        while (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            PermanentChoiceContext.DiscardTriggerAnyTarget pending = gameData.pendingDiscardSelfTriggers.peekFirst();

            // Collect valid targets: all creatures and planeswalkers on all battlefields + all players
            List<UUID> validPermanentTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)
                            || p.getCard().getType() == CardType.PLANESWALKER) {
                        validPermanentTargets.add(p.getId());
                    }
                }
            }

            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players)
            gameData.pendingDiscardSelfTriggers.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.discardedCard().getName() + "'s ability â€” Choose any target.");

            String logEntry = pending.discardedCard().getName() + "'s discard trigger â€” choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discard trigger awaiting target selection", gameData.id, pending.discardedCard().getName());
            return;
        }
    }
}

