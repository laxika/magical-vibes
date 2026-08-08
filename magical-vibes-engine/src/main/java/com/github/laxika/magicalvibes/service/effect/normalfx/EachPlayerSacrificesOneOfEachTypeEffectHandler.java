package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesOneOfEachTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the resolution-time choices for {@link EachPlayerSacrificesOneOfEachTypeEffect}. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesOneOfEachTypeEffectHandler implements NormalEffectHandlerBean {

    private static final List<CardType> TYPES = List.of(
            CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.LAND,
            CardType.PLANESWALKER);

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesOneOfEachTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, apnapPlayers(gameData), 0, 0, List.of(), List.of(), entry.getCard().getName());
    }

    /** Continues the current player's type choices after a multi-select answer. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerSacrificeOneOfEachTypeChoice context) {
        List<UUID> currentPlayerIds = new ArrayList<>(context.currentPlayerIds());
        currentPlayerIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex(), context.typeIndex() + 1,
                context.accumulatedIds(), currentPlayerIds, context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex, int typeIndex,
                      List<UUID> accumulatedIds, List<UUID> currentPlayerIds, String sourceName) {
        List<UUID> allAccumulatedIds = new ArrayList<>(accumulatedIds);
        List<UUID> currentIds = new ArrayList<>(currentPlayerIds);
        int currentPlayerIndex = playerIndex;
        int currentTypeIndex = typeIndex;

        while (currentPlayerIndex < playerIds.size()) {
            UUID subjectPlayerId = playerIds.get(currentPlayerIndex);

            while (currentTypeIndex < TYPES.size()) {
                CardType type = TYPES.get(currentTypeIndex);
                List<UUID> candidates = candidates(gameData, subjectPlayerId, type);
                currentTypeIndex++;

                if (candidates.isEmpty()) {
                    continue;
                }

                if (candidates.size() == 1) {
                    currentIds.add(candidates.getFirst());
                    continue;
                }

                playerInputService.beginMultiPermanentChoice(gameData, subjectPlayerId, candidates, 1,
                        new MultiPermanentChoiceContext.EachPlayerSacrificeOneOfEachTypeChoice(
                                playerIds, currentPlayerIndex, currentTypeIndex - 1, allAccumulatedIds,
                                currentIds, sourceName),
                        sourceName + " — choose an " + type.name().toLowerCase() + " to sacrifice.");
                return;
            }

            allAccumulatedIds.addAll(currentIds);
            currentIds = new ArrayList<>();
            currentPlayerIndex++;
            currentTypeIndex = 0;
        }

        sacrificeChosenPermanents(gameData, allAccumulatedIds, sourceName);
    }

    private List<UUID> candidates(GameData gameData, UUID playerId, CardType type) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> matchesType(gameData, permanent, type));
    }

    private boolean matchesType(GameData gameData, Permanent permanent, CardType type) {
        return switch (type) {
            case ARTIFACT -> gameQueryService.isArtifact(gameData, permanent);
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
            case LAND -> gameQueryService.isLand(gameData, permanent);
            case PLANESWALKER -> gameQueryService.isPlaneswalker(gameData, permanent);
            default -> false;
        };
    }

    private void sacrificeChosenPermanents(GameData gameData, List<UUID> ids, String sourceName) {
        List<UUID> uniqueIds = new ArrayList<>(new java.util.LinkedHashSet<>(ids));
        if (uniqueIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but nobody sacrifices a permanent."));
            return;
        }
        destructionSupport.performSimultaneousSacrifice(gameData, uniqueIds);
        log.info("Game {} - {} sacrifices {} permanents", gameData.id, sourceName, uniqueIds.size());
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
