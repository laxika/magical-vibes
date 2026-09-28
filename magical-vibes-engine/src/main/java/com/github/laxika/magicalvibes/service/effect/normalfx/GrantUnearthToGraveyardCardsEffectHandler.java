package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantUnearthToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantUnearthToGraveyardCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantUnearthToGraveyardCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantUnearthToGraveyardCardsEffect) effect;
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
                    gameData.cardsGrantedUnearthUntilEndOfTurn.put(card.getId(), grant.unearthCost());
                    count++;
                }
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" grants unearth to " + count + " card(s) in graveyard until end of turn.")
                .build());
        log.info("Game {} - {} grants unearth to {} graveyard card(s)",
                gameData.id, entry.getCard().getName(), count);
    }
}
