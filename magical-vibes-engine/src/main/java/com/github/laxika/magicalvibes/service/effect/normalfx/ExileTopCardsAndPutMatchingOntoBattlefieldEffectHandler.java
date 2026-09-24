package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a fixed top-library exile that returns all matching cards to the battlefield. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsAndPutMatchingOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AuspiciousStarrixSupport auspiciousStarrixSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsAndPutMatchingOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (ExileTopCardsAndPutMatchingOntoBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);

        if (typedEffect.count() <= 0 || library == null || library.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>();
        int exiledCount = 0;
        while (exiledCount < typedEffect.count() && !library.isEmpty()) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            exiledCount++;
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.filter(), entry.getCard().getId(), gameData, controllerId)) {
                matchingCards.add(card);
            }
        }

        if (!matchingCards.isEmpty()) {
            auspiciousStarrixSupport.begin(gameData, controllerId, matchingCards);
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " exiles " + exiledCount
                        + " card" + (exiledCount == 1 ? "" : "s") + " from the top of their library with "
                        + entry.getCard().getName() + "."));
        log.info("Game {} - {} exiled {} cards and put {} matching cards onto the battlefield with {}",
                gameData.id, gameData.playerIdToName.get(controllerId), exiledCount,
                matchingCards.size(), entry.getCard().getName());
    }
}
