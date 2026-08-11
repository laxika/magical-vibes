package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerReturnsCardsFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect) effect;

        Map<UUID, List<Card>> cardsToReturn = new LinkedHashMap<>();
        Map<UUID, List<String>> returnedNamesByPlayer = new LinkedHashMap<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                if (graveyard == null || graveyard.isEmpty()) {
                    continue;
                }

                Set<UUID> trackedIds = e.fromBattlefieldThisTurn()
                        ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(playerId, Set.of())
                        : null;
                List<Card> matching = new ArrayList<>();
                for (Card card : graveyard) {
                    if ((trackedIds == null || trackedIds.contains(card.getId()))
                            && predicateEvaluationService.matchesCardPredicate(card, e.filter(), null)) {
                        matching.add(card);
                    }
                }

                if (matching.isEmpty()) {
                    String playerName = gameData.playerIdToName.get(playerId);
                    String filterLabel = CardPredicateUtils.describeFilter(e.filter());
                    gameLogService.append(gameData,
                            GameLog.text(playerName + " has no " + filterLabel + "s in their graveyard."));
                    continue;
                }

                if (matching.size() <= e.maxCount()) {
                    List<Card> removed = new ArrayList<>();
                    for (Card card : matching) {
                        graveyard.remove(card);
                        graveyardService.notifyCardsLeftGraveyard(gameData, playerId, card);
                        removed.add(card);
                    }
                    cardsToReturn.put(playerId, removed);
                    returnedNamesByPlayer.put(playerId, removed.stream().map(Card::getName).toList());
                } else {
                    gameData.pendingGraveyardReturnQueue.add(
                            new PendingGraveyardReturnChoice(playerId, e.maxCount(), e.filter(),
                                    GraveyardChoiceDestination.BATTLEFIELD, true,
                                    false, e.fromBattlefieldThisTurn()));
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        graveyardReturnSupport.putCardsOntoBattlefieldSimultaneously(
                gameData, cardsToReturn, e.enterTapped(), e.enterWithCounter());
        for (Map.Entry<UUID, List<String>> returned : returnedNamesByPlayer.entrySet()) {
            String playerName = gameData.playerIdToName.get(returned.getKey());
            gameLogService.append(gameData,
                    GameLog.text(playerName + " returns " + String.join(", ", returned.getValue())
                            + " from graveyard to the battlefield."));
        }

        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
        }
    }
}
