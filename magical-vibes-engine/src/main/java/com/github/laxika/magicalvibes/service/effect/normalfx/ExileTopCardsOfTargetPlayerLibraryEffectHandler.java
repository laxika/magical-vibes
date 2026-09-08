package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTopCardsOfTargetPlayerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfTargetPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsOfTargetPlayerLibraryEffect exileEffect =
                (ExileTopCardsOfTargetPlayerLibraryEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, exileEffect.count(),
                AmountContext.forStackEntry(entry, null)));
        int toExile = Math.min(count, deck.size());
        for (int i = 0; i < toExile; i++) {
            exileService.exileCard(gameData, targetPlayerId, deck.removeFirst());
        }
        if (toExile > 0) {
            String targetName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(
                    targetName + " exiles " + toExile + " card(s) from the top of their library."));
        }
    }
}
