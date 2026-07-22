package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KillingWaveEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link KillingWaveEffect}: for each creature, its controller sacrifices it unless they
 * pay X life. APNAP choice order, then simultaneous pay/sacrifice (see card rulings).
 *
 * <p>Each affected player multi-selects creatures to keep (pay X life each), capped by what they can
 * afford. Choices accumulate; after the last player, life is paid and non-kept creatures are
 * sacrificed together. X=0 keeps everything with no prompt (paying 0 life is free).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KillingWaveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KillingWaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int xValue = Math.max(0, entry.getXValue());
        String sourceName = entry.getCard().getName();

        if (xValue == 0) {
            // Paying 0 life keeps every creature — nothing to do.
            return;
        }

        List<UUID> playersWithCreatures = apnapPlayersWithCreatures(gameData);
        if (playersWithCreatures.isEmpty()) {
            return;
        }

        beginNextPlayer(gameData, playersWithCreatures, List.of(), xValue, sourceName);
    }

    /**
     * Prompt the next player who still needs to choose, or apply all decisions when the queue is
     * empty. Players who can't afford any payment auto-keep none (all their creatures will be
     * sacrificed) with no prompt.
     */
    public void beginNextPlayer(GameData gameData, List<UUID> remainingPlayerIds,
            List<UUID> accumulatedKeepIds, int xValue, String sourceName) {
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        playerName + " can't pay " + xValue + " life for any creature (" + sourceName + ")."));
                log.info("Game {} - {} can't afford any Killing Wave payments", gameData.id, playerName);
                continue;
            }

            List<UUID> rest = List.copyOf(remaining);
            playerInputService.beginMultiPermanentChoice(gameData, playerId, creatureIds, maxKeep,
                    new MultiPermanentChoiceContext.KillingWaveKeep(
                            playerId, xValue, sourceName, rest, List.copyOf(accumulatedKeepIds)),
                    sourceName + " — choose creatures to keep (pay " + xValue + " life each).");
            return;
        }

        applyDecisions(gameData, accumulatedKeepIds, xValue, sourceName);
    }

    /** Choice completion: record keeps, then prompt the next player or apply. */
    public void completeKeepChoice(GameData gameData, List<UUID> chosenKeepIds,
            MultiPermanentChoiceContext.KillingWaveKeep context) {
        List<UUID> allKeeps = new ArrayList<>(context.accumulatedKeepIds());
        allKeeps.addAll(chosenKeepIds);

        String playerName = gameData.playerIdToName.get(context.choosingPlayerId());
        if (chosenKeepIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " pays no life (" + context.sourceName() + ")."));
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " will pay " + (chosenKeepIds.size() * context.xValue())
                            + " life to keep " + chosenKeepIds.size() + " creature"
                            + (chosenKeepIds.size() == 1 ? "" : "s")
                            + " (" + context.sourceName() + ")."));
        }

        beginNextPlayer(gameData, context.remainingPlayerIds(), allKeeps,
                context.xValue(), context.sourceName());
    }

    /**
     * Simultaneous resolution: each player pays X life per kept creature they control, then every
     * non-kept creature is sacrificed.
     */
    private void applyDecisions(GameData gameData, List<UUID> keepIds, int xValue, String sourceName) {
        Set<UUID> keepSet = new HashSet<>(keepIds);

        // Pay life first (all players), then sacrifice — both are simultaneous; order is
        // outcome-equivalent for triggers that wait until the spell finishes.
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
            if (!gameQueryService.canPlayerLifeChange(gameData, playerId)
                    || gameData.getLife(playerId) < lifeCost) {
                // Life became unpayable since the choice — treat as unpaid: drop keeps for this player.
                if (battlefield != null) {
                    for (Permanent perm : List.copyOf(battlefield)) {
                        keepSet.remove(perm.getId());
                    }
                }
                continue;
            }
            int currentLife = gameData.getLife(playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - lifeCost);
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " pays " + lifeCost + " life (" + sourceName + ")."));
            log.info("Game {} - {} pays {} life for Killing Wave", gameData.id, playerName, lifeCost);
        }

        List<UUID> toSacrifice = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !keepSet.contains(perm.getId())) {
                    toSacrifice.add(perm.getId());
                }
            }
        });
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
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
