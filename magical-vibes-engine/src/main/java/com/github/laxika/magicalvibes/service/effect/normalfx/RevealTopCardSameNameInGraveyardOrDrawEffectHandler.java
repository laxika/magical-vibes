package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardSameNameInGraveyardOrDrawEffect;
import com.github.laxika.magicalvibes.model.filter.CardNameInControllerGraveyardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardSameNameInGraveyardOrDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardSameNameInGraveyardOrDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library (" + sourceName + ").").build());

        boolean nameMatches = predicateEvaluationService.matchesCardPredicate(
                topCard, new CardNameInControllerGraveyardPredicate(), entry.getCard().getId(), gameData, controllerId);
        if (nameMatches) {
            deck.removeFirst();
            if (graveyardService.addCardToGraveyard(gameData, controllerId, topCard, Zone.LIBRARY)) {
                gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(topCard)
                        .text(" into their graveyard (" + sourceName + ").").build());
            }
            log.info("Game {} - {} puts revealed {} into their graveyard ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
            return;
        }

        playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
        log.info("Game {} - {} draws the revealed {} ({})",
                gameData.id, playerName, topCard.getName(), sourceName);
    }
}
