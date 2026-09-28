package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardPredicateMayPlayWhileSourceControlledEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileUntilCardPredicateMayPlayWhileSourceControlledEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUntilCardPredicateMayPlayWhileSourceControlledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty - no cards are exiled."));
            return;
        }

        ExileUntilCardPredicateMayPlayWhileSourceControlledEffect typedEffect =
                (ExileUntilCardPredicateMayPlayWhileSourceControlledEffect) effect;
        List<Card> exiledCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            exiledCards.add(card);
            exileService.exileCard(gameData, controllerId, card, sourcePermanentId);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                foundCard = card;
                break;
            }
        }

        gameLogService.append(gameData, GameLog.text(
                playerName + " exiles " + exiledCards.stream().map(Card::getName).toList()
                        + " from the top of their library."));
        if (foundCard == null) {
            return;
        }

        exiledCards.remove(foundCard);
        exiledCards.forEach(card -> gameData.removeFromExile(card.getId()));
        Collections.shuffle(exiledCards);
        deck.addAll(exiledCards);

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null && controllerId.equals(gameData.findControllerOf(source))) {
            gameData.exilePlayPermissions.put(foundCard.getId(), controllerId);
            gameData.exilePlayPermissionSourcePermanents.put(foundCard.getId(), sourcePermanentId);
        }
        log.info("Game {} - {} may play {} from exile while controlling {}",
                gameData.id, playerName, foundCard.getName(), entry.getCard().getName());
    }
}
