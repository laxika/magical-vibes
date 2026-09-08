package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves each player's power-limited keep choice before the simultaneous sacrifices. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var powerLimit = (EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect) effect;
        beginNextPlayer(gameData, apnapPlayers(gameData), List.of(), powerLimit.maxPower(),
                entry.getCard().getName());
    }

    /** Records one player's choice and continues with the next player or the final sacrifice. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice context) {
        List<UUID> accumulatedKeepIds = new ArrayList<>(context.accumulatedKeepIds());
        accumulatedKeepIds.addAll(chosenIds);
        beginNextPlayer(gameData, context.remainingPlayerIds(), accumulatedKeepIds,
                context.maxPower(), context.sourceName());
    }

    private void beginNextPlayer(GameData gameData, List<UUID> remainingPlayerIds,
            List<UUID> accumulatedKeepIds, int maxPower, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        while (!remaining.isEmpty()) {
            UUID playerId = remaining.removeFirst();
            List<UUID> creatureIds = collectCreatureIds(gameData, playerId);
            if (creatureIds.isEmpty()) {
                continue;
            }

            playerInputService.beginMultiPermanentChoice(gameData, playerId, creatureIds, creatureIds.size(),
                    new MultiPermanentChoiceContext.EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice(
                            playerId, maxPower, List.copyOf(remaining), List.copyOf(accumulatedKeepIds), sourceName),
                    sourceName + " — choose creatures with total power " + maxPower + " or less to keep.");
            return;
        }

        sacrificeRest(gameData, accumulatedKeepIds, sourceName);
    }

    private void sacrificeRest(GameData gameData, List<UUID> keptIds, String sourceName) {
        Set<UUID> kept = new HashSet<>(keptIds);
        List<UUID> toSacrifice = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent) && !kept.contains(permanent.getId())) {
                    toSacrifice.add(permanent.getId());
                }
            }
        });

        if (toSacrifice.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but no creatures are sacrificed."));
            return;
        }
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        log.info("Game {} - {} sacrifices {} creatures", gameData.id, sourceName, toSacrifice.size());
    }

    private List<UUID> collectCreatureIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectCreatureIds(gameData, playerId, p -> true);
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
