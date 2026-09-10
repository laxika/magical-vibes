package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect exileEffect =
                (ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, exileEffect.count(),
                AmountContext.forStackEntry(entry, null)));
        int toExile = Math.min(count, library.size());
        UUID controllerId = entry.getControllerId();
        List<Card> exiled = new ArrayList<>();
        for (int i = 0; i < toExile; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, targetPlayerId, card);
            gameData.exilePlayPermissions.put(card.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            gameData.exilePlayForLifeEqualToManaValue.add(card.getId());
            exiled.add(card);
        }

        if (!exiled.isEmpty()) {
            String targetName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(targetName + " exiles "
                    + exiled.size() + " card(s) from the top of their library; they may be played this turn."));
        }
    }
}
