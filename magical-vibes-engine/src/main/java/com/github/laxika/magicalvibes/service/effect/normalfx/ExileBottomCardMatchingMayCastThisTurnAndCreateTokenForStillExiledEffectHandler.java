package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves bottom-of-library creature exile with a temporary cast permission. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card matchingCard = null;
        for (int index = library.size() - 1; index >= 0; index--) {
            Card card = library.get(index);
            if (predicateEvaluationService.matchesCardPredicate(card, exileEffect.filter(), null)) {
                matchingCard = library.remove(index);
                break;
            }
        }
        if (matchingCard == null) {
            return;
        }

        exileService.exileCard(gameData, controllerId, matchingCard);
        if (gameData.findExiledCard(matchingCard.getId()) == null) {
            return;
        }

        gameData.grantExilePlayPermission(matchingCard.getId(), controllerId, Duration.UNTIL_END_OF_TURN);
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                .card(matchingCard)
                .text(" from the bottom of their library (may cast it this turn).")
                .build());

        gameData.temporaryGlobalTriggeredAbilities.add(
                PutStillExiledCardsIntoGraveyardAndCreateTokensEffectHandler.atNextEndStep(
                gameData, controllerId, entry.getCard(), entry.getSourcePermanentId(),
                List.of(matchingCard.getId()), exileEffect.tokenEffect()));
        log.info("Game {} - {} exiled {} from the bottom of their library",
                gameData.id, gameData.playerIdToName.get(controllerId), matchingCard.getName());
    }
}
