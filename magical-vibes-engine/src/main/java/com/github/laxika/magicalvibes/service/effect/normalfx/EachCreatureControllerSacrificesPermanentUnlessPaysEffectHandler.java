package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachCreatureControllerSacrificesPermanentUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
 * Resolves the APNAP choices for Fade Away-style pay-or-sacrifice effects.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachCreatureControllerSacrificesPermanentUnlessPaysEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachCreatureControllerSacrificesPermanentUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var fadeAway = (EachCreatureControllerSacrificesPermanentUnlessPaysEffect) effect;
        beginNextPlayer(gameData, apnapPlayersWithCreatures(gameData), List.of(), List.of(),
                fadeAway.manaCost(), entry.getControllerId(), entry.getCard().getName());
    }

    public void completeKeepChoice(GameData gameData, List<UUID> chosenKeepIds,
            MultiPermanentChoiceContext.FadeAwayKeep context) {
        List<UUID> keptIds = new ArrayList<>(context.accumulatedKeepIds());
        keptIds.addAll(chosenKeepIds);

        List<UUID> creatureIds = context.creatureIds();
        Set<UUID> chosenKeeps = new HashSet<>(chosenKeepIds);
        int requiredSacrifices = (int) creatureIds.stream()
                .filter(creatureId -> !chosenKeeps.contains(creatureId))
                .count();
        beginSacrificeChoice(gameData, context.choosingPlayerId(), creatureIds, requiredSacrifices,
                context.remainingPlayerIds(), keptIds, context.accumulatedSacrificeIds(),
                context.manaCost(), context.sourceControllerId(), context.sourceName());
    }

    public void completeSacrificeChoice(GameData gameData, List<UUID> chosenSacrificeIds,
            MultiPermanentChoiceContext.FadeAwaySacrifice context) {
        List<UUID> sacrificedIds = new ArrayList<>(context.accumulatedSacrificeIds());
        sacrificedIds.addAll(chosenSacrificeIds);
        beginNextPlayer(gameData, context.remainingPlayerIds(), context.accumulatedKeepIds(),
                sacrificedIds, context.manaCost(), context.sourceControllerId(), context.sourceName());
    }

    private void beginNextPlayer(GameData gameData, List<UUID> remainingPlayerIds,
            List<UUID> accumulatedKeepIds, List<UUID> accumulatedSacrificeIds, String manaCost,
            UUID sourceControllerId, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        while (!remaining.isEmpty()) {
            UUID playerId = remaining.removeFirst();
            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
                continue;
            }

            List<UUID> creatureIds = collectCreatureIds(gameData, playerId);
            if (creatureIds.isEmpty()) {
                continue;
            }

            int maxKeeps = maxAffordableKeeps(gameData, playerId, manaCost, creatureIds.size());
            if (maxKeeps == 0) {
                beginSacrificeChoice(gameData, playerId, creatureIds, creatureIds.size(), remaining,
                        accumulatedKeepIds, accumulatedSacrificeIds, manaCost, sourceControllerId,
                        sourceName);
                return;
            }

            playerInputService.beginMultiPermanentChoice(gameData, playerId, creatureIds, maxKeeps,
                    new MultiPermanentChoiceContext.FadeAwayKeep(playerId, creatureIds, remaining,
                            accumulatedKeepIds, accumulatedSacrificeIds, sourceControllerId,
                            sourceName, manaCost),
                    sourceName + " — choose creatures to pay for (" + manaCost + " each).");
            return;
        }

        applyDecisions(gameData, accumulatedKeepIds, accumulatedSacrificeIds, manaCost, sourceName);
    }

    private void beginSacrificeChoice(GameData gameData, UUID playerId, List<UUID> creatureIds,
            int requiredSacrifices, List<UUID> remainingPlayerIds, List<UUID> accumulatedKeepIds,
            List<UUID> accumulatedSacrificeIds, String manaCost, UUID sourceControllerId,
            String sourceName) {
        Set<UUID> keepIds = new HashSet<>(accumulatedKeepIds);
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!keepIds.contains(permanent.getId())) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (requiredSacrifices == 0) {
            beginNextPlayer(gameData, remainingPlayerIds, accumulatedKeepIds,
                    accumulatedSacrificeIds, manaCost, sourceControllerId, sourceName);
            return;
        }
        if (validIds.size() <= requiredSacrifices) {
            List<UUID> sacrifices = new ArrayList<>(accumulatedSacrificeIds);
            sacrifices.addAll(validIds);
            beginNextPlayer(gameData, remainingPlayerIds, accumulatedKeepIds, sacrifices,
                    manaCost, sourceControllerId, sourceName);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, playerId, validIds, requiredSacrifices,
                new MultiPermanentChoiceContext.FadeAwaySacrifice(playerId, creatureIds,
                        requiredSacrifices, remainingPlayerIds, accumulatedKeepIds,
                        accumulatedSacrificeIds, sourceControllerId, sourceName, manaCost),
                sourceName + " — choose " + requiredSacrifices + " permanent"
                        + (requiredSacrifices == 1 ? "" : "s") + " to sacrifice.");
    }

    private void applyDecisions(GameData gameData, List<UUID> keepIds,
            List<UUID> sacrificeIds, String manaCost, String sourceName) {
        Set<UUID> finalKeepIds = new HashSet<>(keepIds);
        List<UUID> finalSacrificeIds = new ArrayList<>(sacrificeIds);

        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool == null) {
                continue;
            }
            for (UUID keepId : new ArrayList<>(finalKeepIds)) {
                Permanent keep = gameQueryService.findPermanentById(gameData, keepId);
                if (keep == null || !playerId.equals(gameQueryService.findPermanentController(gameData, keepId))) {
                    continue;
                }
                ManaCost cost = new ManaCost(manaCost);
                if (cost.canPay(pool)) {
                    cost.pay(pool);
                    gameLogService.append(gameData, GameLog.text(
                            gameData.playerIdToName.get(playerId) + " pays " + manaCost + " for "
                                    + sourceName + "."));
                } else {
                    finalKeepIds.remove(keepId);
                    finalSacrificeIds.add(keepId);
                }
            }
        }

        destructionSupport.performSimultaneousSacrifice(gameData, finalSacrificeIds);
        log.info("Game {} - {} resolves with {} sacrifices", gameData.id, sourceName,
                finalSacrificeIds.size());
    }

    private int maxAffordableKeeps(GameData gameData, UUID playerId, String manaCost,
            int creatureCount) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool == null) {
            return 0;
        }
        ManaPool simulatedPool = new ManaPool(pool);
        int keeps = 0;
        while (keeps < creatureCount) {
            ManaCost cost = new ManaCost(manaCost);
            if (!cost.canPay(simulatedPool)) {
                break;
            }
            cost.pay(simulatedPool);
            keeps++;
        }
        return keeps;
    }

    private List<UUID> collectCreatureIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private List<UUID> apnapPlayersWithCreatures(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return ordered.stream()
                .filter(playerId -> !collectCreatureIds(gameData, playerId).isEmpty())
                .toList();
    }
}
