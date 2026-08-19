package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the resolution-time choices for {@link EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffect}. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffectHandler
        implements NormalEffectHandlerBean {

    private static final List<CardSubtype> BASIC_LAND_TYPES = List.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, apnapPlayers(gameData), 0, 0, List.of(), entry.getCard().getName());
    }

    /** Continues the current player's choices after a land selection. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice context) {
        List<UUID> selectedIds = new ArrayList<>(context.selectedIds());
        selectedIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex(), context.typeIndex() + 1,
                selectedIds, context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex, int typeIndex,
                      List<UUID> selectedIds, String sourceName) {
        List<UUID> allSelectedIds = new ArrayList<>(selectedIds);
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
                    allSelectedIds.add(candidates.getFirst());
                    continue;
                }

                playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, 1,
                        new MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice(
                                playerIds, currentPlayerIndex, currentTypeIndex - 1, allSelectedIds, sourceName),
                        sourceName + " — choose a " + type.name().toLowerCase() + " to return to its owner's hand.");
                return;
            }

            currentPlayerIndex++;
            currentTypeIndex = 0;
        }

        returnSelectedLands(gameData, allSelectedIds, sourceName);
    }

    private List<UUID> candidates(GameData gameData, UUID playerId, CardSubtype type) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isLand(gameData, permanent)
                        && gameQueryService.effectiveBasicLandTypes(gameData, permanent).contains(type))
                .map(Permanent::getId)
                .toList();
    }

    private void returnSelectedLands(GameData gameData, List<UUID> selectedIds, String sourceName) {
        for (UUID selectedId : selectedIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, selectedId);
            if (permanent == null) {
                continue;
            }
            Card card = permanent.getCard();
            if (permanentRemovalService.removePermanentToHand(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.cardThen(card, " is returned to its owner's hand."));
                log.info("Game {} - {} returns {} to its owner's hand", gameData.id, sourceName,
                        card.getName());
            }
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
