package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowPlayMatchingCardsFromGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayMatchingCardsFromGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AllowPlayMatchingCardsFromGraveyardThisTurnEffect permissionEffect =
                (AllowPlayMatchingCardsFromGraveyardThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : List.copyOf(graveyard)) {
                if (predicateEvaluationService.matchesCardPredicate(card, permissionEffect.filter(), null)) {
                    gameData.graveyardPlayPermissions.put(card.getId(), controllerId);
                    gameData.graveyardPlayPermissionsExpireEndOfTurn.add(card.getId());
                }
            }
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may play matching cards from their graveyard this turn."));
        log.info("Game {} - {} may play matching cards from their graveyard this turn",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }
}
