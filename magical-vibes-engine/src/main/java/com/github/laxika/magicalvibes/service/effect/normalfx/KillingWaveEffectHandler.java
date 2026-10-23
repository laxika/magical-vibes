package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KillingWaveEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link KillingWaveEffect}: for each creature, its controller sacrifices it unless they
 * pay X life. APNAP choice order, then simultaneous pay/sacrifice (see card rulings).
 *
 * <p>Each affected player multi-selects creatures to keep (pay X life each), capped by what they can
 * afford. Choices accumulate; after the last player, life is paid and non-kept creatures are
 * sacrificed together. At X=0 the choice is still offered: paying 0 life is free (CR 119.4), but
 * the controller may still decline to pay and sacrifice the creature.
 *
 * <p>Nothing changes before that sacrifice: the "will pay" and "can't pay" log lines are written
 * only once the next prompt opens or once the batch's death replacements have been asked
 * (CR 616.1), and life is paid only after that, so a resolution or choice completion stopped by
 * that question and run again logs and pays once.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KillingWaveEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;
    @Autowired @Lazy
    private TriggerCollectionService triggerCollectionService;
    @Autowired @Lazy
    private LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KillingWaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int xValue = Math.max(0, entry.getXValue());
        String sourceName = entry.getCard().getName();

        List<UUID> playersWithCreatures = apnapPlayersWithCreatures(gameData);
        if (playersWithCreatures.isEmpty()) {
            return;
        }

        beginNextPlayer(gameData, playersWithCreatures, List.of(), xValue, sourceName,
                entry.getControllerId());
    }

    /**
     * Prompt the next player who still needs to choose, or apply all decisions when the queue is
     * empty. Players who can't afford any payment auto-keep none (all their creatures will be
     * sacrificed) with no prompt.
     */
    public void beginNextPlayer(GameData gameData, List<UUID> remainingPlayerIds,
            List<UUID> accumulatedKeepIds, int xValue, String sourceName, UUID sourceControllerId) {
        beginNextPlayer(gameData, remainingPlayerIds, accumulatedKeepIds, xValue, sourceName,
                sourceControllerId, List.of());
    }

    /** {@code heldLogs} are log lines of earlier decisions, written once nothing can stop them. */
    private void beginNextPlayer(GameData gameData, List<UUID> remainingPlayerIds,
            List<UUID> accumulatedKeepIds, int xValue, String sourceName, UUID sourceControllerId,
            List<GameLogEntry> heldLogs) {
        List<GameLogEntry> logs = new ArrayList<>(heldLogs);
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        while (!remaining.isEmpty()) {
            UUID playerId = remaining.removeFirst();
            List<UUID> creatureIds = collectCreatureIds(gameData, playerId);
            if (creatureIds.isEmpty()) {
                continue;
            }

            int maxKeep = maxAffordableKeeps(gameData, playerId, xValue, creatureIds.size());
            if (maxKeep <= 0) {
                // Can't pay for any — all will be sacrificed; no choice to make.
                String playerName = gameData.playerIdToName.get(playerId);
                logs.add(GameLog.text(
                        playerName + " can't pay " + xValue + " life for any creature (" + sourceName + ")."));
                log.info("Game {} - {} can't afford any Killing Wave payments", gameData.id, playerName);
                continue;
            }

            List<UUID> rest = List.copyOf(remaining);
            appendAll(gameData, logs);
            playerInputService.beginMultiPermanentChoice(gameData, playerId, creatureIds, maxKeep,
                    new MultiPermanentChoiceContext.KillingWaveKeep(
                            playerId, xValue, sourceName, rest, List.copyOf(accumulatedKeepIds),
                            sourceControllerId),
                    sourceName + " — choose creatures to keep (pay " + xValue + " life each).");
            return;
        }

        applyDecisions(gameData, accumulatedKeepIds, xValue, sourceName, sourceControllerId, logs);
    }

    /** Choice completion: record keeps, then prompt the next player or apply. */
    public void completeKeepChoice(GameData gameData, List<UUID> chosenKeepIds,
            MultiPermanentChoiceContext.KillingWaveKeep context) {
        List<UUID> allKeeps = new ArrayList<>(context.accumulatedKeepIds());
        allKeeps.addAll(chosenKeepIds);

        String playerName = gameData.playerIdToName.get(context.choosingPlayerId());
        GameLogEntry decisionLog;
        if (chosenKeepIds.isEmpty()) {
            decisionLog = GameLog.text(playerName + " pays no life (" + context.sourceName() + ").");
        } else {
            decisionLog = GameLog.text(
                    playerName + " will pay " + (chosenKeepIds.size() * context.xValue())
                            + " life to keep " + chosenKeepIds.size() + " creature"
                            + (chosenKeepIds.size() == 1 ? "" : "s")
                            + " (" + context.sourceName() + ").");
        }

        beginNextPlayer(gameData, context.remainingPlayerIds(), allKeeps,
                context.xValue(), context.sourceName(), context.sourceControllerId(), List.of(decisionLog));
    }

    /**
     * Simultaneous resolution: each player pays X life per kept creature they control, then every
     * non-kept creature is sacrificed.
     */
    private void applyDecisions(GameData gameData, List<UUID> keepIds, int xValue, String sourceName,
            UUID sourceControllerId, List<GameLogEntry> heldLogs) {
        Set<UUID> keepSet = new HashSet<>(keepIds);
        Map<UUID, Integer> lifeCosts = new LinkedHashMap<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            int kept = 0;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent perm : battlefield) {
                    if (keepSet.contains(perm.getId()) && gameQueryService.isCreature(gameData, perm)) {
                        kept++;
                    }
                }
            }
            if (kept <= 0) {
                continue;
            }
            int lifeCost = kept * xValue;
            if (lifeCost > 0 && (!gameQueryService.canPlayerLifeChange(gameData, playerId)
                    || gameData.getLife(playerId) < lifeCost)) {
                // Life became unpayable since the choice — treat as unpaid: drop keeps for this player.
                if (battlefield != null) {
                    for (Permanent perm : List.copyOf(battlefield)) {
                        keepSet.remove(perm.getId());
                    }
                }
                continue;
            }
            lifeCosts.put(playerId, lifeCost);
        }

        List<Permanent> toSacrifice = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !keepSet.contains(perm.getId())
                        && gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
                    toSacrifice.add(perm);
                }
            }
        });

        // CR 616.1: ask the batch's death replacements before anything is logged or paid, so a rerun does it once.
        destructionSupport.askDeathReplacementsBeforeSacrifice(gameData, toSacrifice);
        appendAll(gameData, heldLogs);

        // Pay life first (all players), then sacrifice — both are simultaneous; order is
        // outcome-equivalent for triggers that wait until the spell finishes.
        for (Map.Entry<UUID, Integer> payment : lifeCosts.entrySet()) {
            UUID playerId = payment.getKey();
            int lifeCost = payment.getValue();
            lifeSupport.applyLifePayment(gameData, playerId, lifeCost, sourceName);
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays " + lifeCost + " life (" + sourceName + ")."));
            log.info("Game {} - {} pays {} life for Killing Wave", gameData.id, playerName, lifeCost);
        }

        destructionSupport.performSimultaneousSacrifice(gameData,
                toSacrifice.stream().map(Permanent::getId).toList());
    }

    private void appendAll(GameData gameData, List<GameLogEntry> logs) {
        for (GameLogEntry logEntry : logs) {
            gameLogService.append(gameData, logEntry);
        }
    }

    private int maxAffordableKeeps(GameData gameData, UUID playerId, int xValue, int creatureCount) {
        if (xValue <= 0) {
            return creatureCount;
        }
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            return 0;
        }
        int life = gameData.getLife(playerId);
        return Math.min(creatureCount, life / xValue);
    }

    private List<UUID> collectCreatureIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectCreatureIds(gameData, playerId, p -> true);
    }

    /** Players who control at least one creature, in APNAP order (active player first). */
    private List<UUID> apnapPlayersWithCreatures(GameData gameData) {
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
            if (!collectCreatureIds(gameData, id).isEmpty()) {
                result.add(id);
            }
        }
        return result;
    }
}
