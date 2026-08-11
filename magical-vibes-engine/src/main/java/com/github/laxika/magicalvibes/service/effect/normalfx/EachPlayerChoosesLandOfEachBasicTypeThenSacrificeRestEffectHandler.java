package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffect;
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

/** Resolves the resolution-time choices for {@link EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffect}. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffectHandler
        implements NormalEffectHandlerBean {

    private static final List<CardSubtype> BASIC_LAND_TYPES = List.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, apnapPlayers(gameData), 0, 0, List.of(), entry.getCard().getName());
    }

    /** Continues the current player's choices after a land selection. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeChoice context) {
        List<UUID> keptIds = new ArrayList<>(context.keptIds());
        keptIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex(), context.typeIndex() + 1,
                keptIds, context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex, int typeIndex,
                      List<UUID> keptIds, String sourceName) {
        List<UUID> allKeptIds = new ArrayList<>(keptIds);
        int currentPlayerIndex = playerIndex;
        int currentTypeIndex = typeIndex;

        while (currentPlayerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(currentPlayerIndex);

            while (currentTypeIndex < BASIC_LAND_TYPES.size()) {
                CardSubtype type = BASIC_LAND_TYPES.get(currentTypeIndex);
                List<UUID> candidates = candidates(gameData, playerId, type);
                currentTypeIndex++;

                if (candidates.isEmpty()) {
                    continue;
                }
                if (candidates.size() == 1) {
                    allKeptIds.add(candidates.getFirst());
                    continue;
                }

                playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, 1,
                        new MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeChoice(
                                playerIds, currentPlayerIndex, currentTypeIndex - 1, allKeptIds, sourceName),
                        sourceName + " — choose a " + type.name().toLowerCase() + " to keep.");
                return;
            }

            currentPlayerIndex++;
            currentTypeIndex = 0;
        }

        sacrificeRest(gameData, allKeptIds, sourceName);
    }

    private List<UUID> candidates(GameData gameData, UUID playerId, CardSubtype type) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> gameQueryService.isLand(gameData, permanent)
                        && gameQueryService.effectiveBasicLandTypes(gameData, permanent).contains(type));
    }

    private void sacrificeRest(GameData gameData, List<UUID> keptIds, String sourceName) {
        Set<UUID> kept = new HashSet<>(keptIds);

        List<UUID> toSacrifice = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isLand(gameData, permanent) && !kept.contains(permanent.getId())) {
                    toSacrifice.add(permanent.getId());
                }
            }
        });

        if (toSacrifice.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but nobody sacrifices a land."));
            return;
        }
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        log.info("Game {} - {} sacrifices {} lands", gameData.id, sourceName, toSacrifice.size());
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
