package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves per-player coin flips and the resulting simultaneous sacrifice choices. Every player flips
 * before any result is applied, so a Krark's Thumb choice on any of the flips can stop the effect.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerFlipsCoinAndTailsSacrificesCreatureEffectHandler
        implements NormalEffectHandlerBean, ChoosesCoinFlipResultsFirst {

    private final CoinFlipService coinFlipService;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        String sourceName = entry.getCard().getName();

        List<UUID> players = apnapPlayers(gameData);
        List<CoinFlipService.CoinFlipResult> results = coinFlipService.flipEach(gameData, players);
        for (int index = 0; index < players.size(); index++) {
            UUID playerId = players.get(index);
            CoinFlipService.CoinFlipResult result = results.get(index);
            String playerName = gameData.playerIdToName.get(playerId);
            String outcome = result.heads() ? "wins" : "loses";
            gameLogService.append(gameData, GameLog.text(playerName + " " + outcome
                    + " the coin flip for " + sourceName
                    + coinFlipService.replacementDetails(result) + "."));

            // Heads-or-tails only (CR 705.2): neither result is a win or loss, so no coin-flip win/loss triggers.
            if (result.heads()) {
                continue;
            }

            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }

            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, playerId,
                    permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent));
            if (creatureIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " has no creature to sacrifice."));
            } else if (creatureIds.size() == 1) {
                autoSacrificeIds.add(creatureIds.getFirst());
            } else {
                choosers.add(new PendingForcedSacrifice(playerId, 1, creatureIds));
            }
        }

        if (choosers.isEmpty()) {
            // The coins are already flipped, so the sacrifice is this step's last action: a CR 616.1 question
            // ends the step before any of them leaves.
            if (!autoSacrificeIds.isEmpty()) {
                stepDepartureSupport.depart(gameData, entry, effect,
                        DepartureBatch.of(StepDepartureSupport.sacrifices(autoSacrificeIds)));
            }
        } else {
            destructionSupport.beginNextForcedSacrificeFromQueue(
                    gameData, choosers, autoSacrificeIds, true);
        }
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
