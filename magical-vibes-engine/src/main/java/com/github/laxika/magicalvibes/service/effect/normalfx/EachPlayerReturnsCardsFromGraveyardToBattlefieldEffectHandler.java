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
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerReturnsCardsFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect) effect;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            List<Card> matching = new ArrayList<>();
            for (Card card : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(card, e.filter(), null)) {
                    matching.add(card);
                }
            }

            if (matching.isEmpty()) {
                String playerName = gameData.playerIdToName.get(playerId);
                String filterLabel = CardPredicateUtils.describeFilter(e.filter());
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no " + filterLabel + "s in their graveyard."));
                continue;
            }

            if (matching.size() <= e.maxCount()) {
                // Auto-return all matching cards — no choice needed
                List<String> returnedNames = new ArrayList<>();
                graveyardService.beginGraveyardLeaveBatch(gameData);
                try {
                    for (Card card : matching) {
                        graveyard.remove(card);
                        graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
                        graveyardReturnSupport.putCardOntoBattlefield(gameData, playerId, card,
                                null, null, false, false, e.enterWithCounter());
                        returnedNames.add(card.getName());
                    }
                } finally {
                    graveyardService.endGraveyardLeaveBatch(gameData);
                }
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " returns " + String.join(", ", returnedNames)
                                + " from graveyard to the battlefield."));
            } else {
                // Player must choose — add to queue
                gameData.pendingGraveyardReturnQueue.add(
                        new PendingGraveyardReturnChoice(playerId, e.maxCount(), e.filter(),
                                GraveyardChoiceDestination.BATTLEFIELD, true));
            }
        }

        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
        }
    }
}
