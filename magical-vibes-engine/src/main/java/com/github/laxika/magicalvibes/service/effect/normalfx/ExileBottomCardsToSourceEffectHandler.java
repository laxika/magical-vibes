package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardsToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves source-linked exile from the bottom of a library. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileBottomCardsToSourceEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileBottomCardsToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileBottomCardsToSourceEffect exileEffect = (ExileBottomCardsToSourceEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            log.info("Game {} - Source permanent no longer on battlefield, bottom-card exile fizzles", gameData.id);
            return;
        }

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, exileEffect.count(),
                AmountContext.forStackEntry(entry, source)));
        if (count == 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!exileEffect.eachOpponent() && !playerId.equals(controllerId)) {
                continue;
            }
            if (exileEffect.eachOpponent() && playerId.equals(controllerId)) {
                continue;
            }

            var library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            int exiledCount = Math.min(count, library.size());
            for (int i = 0; i < exiledCount; i++) {
                Card card = library.removeLast();
                if (exileEffect.faceDown()) {
                    exileService.exileCardFaceDown(gameData, playerId, card, sourcePermanentId);
                } else {
                    exileService.exileCard(gameData, playerId, card, sourcePermanentId);
                }
            }

            String visibility = exileEffect.faceDown() ? " face down" : "";
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " exiles " + exiledCount
                            + " card" + (exiledCount == 1 ? "" : "s")
                            + " from the bottom of their library" + visibility + " with ")
                    .card(source.getCard()).text(".").build());
            log.info("Game {} - {} exiles {} cards from library bottom with {}", gameData.id,
                    gameData.playerIdToName.get(playerId), exiledCount, source.getCard().getName());
        }
    }
}
