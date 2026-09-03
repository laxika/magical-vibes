package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a one-permanent choice for every player in active-player order. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerReturnsPermanentToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerReturnsPermanentToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextChoice(gameData, apnapPlayers(gameData), new ArrayList<>(), entry.getCard().getName());
    }

    /** Begins the replacement's choices without creating a stack entry. */
    public void beginReplacement(GameData gameData, String sourceName) {
        beginNextChoice(gameData, apnapPlayers(gameData), new ArrayList<>(), sourceName);
    }

    /** Continues the active-player choice sequence after one player selects a permanent. */
    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.EachPlayerReturnsPermanent context) {
        List<UUID> chosenIds = new ArrayList<>(context.chosenIds());
        chosenIds.add(permanentIds.getFirst());
        beginNextChoice(gameData, context.remainingPlayerIds(), chosenIds, context.sourceName());
    }

    private void beginNextChoice(GameData gameData, List<UUID> remainingPlayerIds,
                                 List<UUID> chosenIds, String sourceName) {
        if (remainingPlayerIds.isEmpty()) {
            returnChosenPermanents(gameData, chosenIds, sourceName);
            return;
        }

        UUID playerId = remainingPlayerIds.getFirst();
        List<UUID> nextRemainingPlayerIds = remainingPlayerIds.size() > 1
                ? List.copyOf(remainingPlayerIds.subList(1, remainingPlayerIds.size()))
                : List.of();
        List<UUID> permanentIds = permanentIds(gameData, playerId);
        if (permanentIds.isEmpty()) {
            beginNextChoice(gameData, nextRemainingPlayerIds, chosenIds, sourceName);
        } else if (permanentIds.size() == 1) {
            List<UUID> nextChosenIds = new ArrayList<>(chosenIds);
            nextChosenIds.add(permanentIds.getFirst());
            beginNextChoice(gameData, nextRemainingPlayerIds, nextChosenIds, sourceName);
        } else {
            playerInputService.beginMultiPermanentChoice(gameData, playerId, permanentIds, 1,
                    new MultiPermanentChoiceContext.EachPlayerReturnsPermanent(nextRemainingPlayerIds,
                            chosenIds, sourceName),
                    sourceName + " — Choose a permanent to return to its owner's hand.");
        }
    }

    private List<UUID> permanentIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream().map(Permanent::getId).toList();
    }

    private void returnChosenPermanents(GameData gameData, List<UUID> chosenIds, String sourceName) {
        for (UUID permanentId : chosenIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
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
