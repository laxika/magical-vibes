package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsUntilPermanentCountToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a dynamic top-library permanent-card count and puts the found cards onto the battlefield. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsUntilPermanentCountToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final AuspiciousStarrixSupport auspiciousStarrixSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsUntilPermanentCountToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (ExileTopCardsUntilPermanentCountToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        int permanentCount = amountEvaluationService.evaluate(
                gameData, typedEffect.permanentCount(), AmountContext.forStackEntry(entry, null));

        if (permanentCount <= 0 || library == null || library.isEmpty()) {
            return;
        }

        List<Card> permanentCards = new ArrayList<>();
        int exiledCount = 0;
        while (!library.isEmpty() && permanentCards.size() < permanentCount) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            exiledCount++;
            if (card.getType().isPermanentType()) {
                permanentCards.add(card);
            }
        }

        auspiciousStarrixSupport.begin(gameData, controllerId, permanentCards);

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " exiles " + exiledCount
                        + " card" + (exiledCount == 1 ? "" : "s") + " from the top of their library with "
                        + entry.getCard().getName() + "."));
        log.info("Game {} - {} exiled {} cards and found {} permanent cards with {}",
                gameData.id, gameData.playerIdToName.get(controllerId), exiledCount,
                permanentCards.size(), entry.getCard().getName());
    }
}
