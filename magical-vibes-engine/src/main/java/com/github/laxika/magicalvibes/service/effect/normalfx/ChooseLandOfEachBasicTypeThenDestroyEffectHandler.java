package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseLandOfEachBasicTypeThenDestroyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Sundering Titan's resolution-time land choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseLandOfEachBasicTypeThenDestroyEffectHandler implements NormalEffectHandlerBean {

    private static final List<CardSubtype> BASIC_LAND_TYPES = List.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseLandOfEachBasicTypeThenDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, entry.getControllerId(), 0, List.of(), entry.getCard().getName());
    }

    /** Continues the controller's choices after selecting a land for one basic land type. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.ChooseLandOfEachBasicTypeThenDestroyChoice context) {
        List<UUID> allChosenIds = new ArrayList<>(context.chosenIds());
        allChosenIds.addAll(chosenIds);
        step(gameData, context.controllerId(), context.typeIndex() + 1,
                allChosenIds, context.sourceName());
    }

    private void step(GameData gameData, UUID controllerId, int typeIndex,
                      List<UUID> chosenIds, String sourceName) {
        List<UUID> allChosenIds = new ArrayList<>(chosenIds);
        int currentTypeIndex = typeIndex;

        while (currentTypeIndex < BASIC_LAND_TYPES.size()) {
            CardSubtype type = BASIC_LAND_TYPES.get(currentTypeIndex);
            List<UUID> candidates = candidates(gameData, type);
            currentTypeIndex++;

            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                allChosenIds.add(candidates.getFirst());
                continue;
            }

            playerInputService.beginMultiPermanentChoice(gameData, controllerId, candidates, 1,
                    new MultiPermanentChoiceContext.ChooseLandOfEachBasicTypeThenDestroyChoice(
                            controllerId, currentTypeIndex - 1, allChosenIds, sourceName),
                    sourceName + " — choose a " + type.name().toLowerCase() + " to destroy.");
            return;
        }

        destroyChosenLands(gameData, allChosenIds, sourceName);
    }

    private List<UUID> candidates(GameData gameData, CardSubtype type) {
        List<UUID> candidates = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            candidates.addAll(destructionSupport.collectPermanentIds(gameData, playerId,
                    permanent -> gameQueryService.isLand(gameData, permanent)
                            && gameQueryService.effectiveBasicLandTypes(gameData, permanent).contains(type)));
        }
        return candidates;
    }

    private void destroyChosenLands(GameData gameData, List<UUID> chosenIds, String sourceName) {
        Set<UUID> uniqueChosenIds = new LinkedHashSet<>(chosenIds);
        List<Permanent> toDestroy = uniqueChosenIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .toList();

        if (toDestroy.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but no lands are destroyed."));
            return;
        }
        destructionSupport.destroyBatch(gameData, toDestroy, sourceName, false);
        log.info("Game {} - {} destroys {} lands", gameData.id, sourceName, toDestroy.size());
    }
}
