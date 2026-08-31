package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect exileEffect =
                (ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int toExile = Math.min(exileEffect.count(), library.size());
        UUID controllerId = entry.getControllerId();
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        for (int i = 0; i < toExile; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, targetPlayerId, card);
            gameData.exilePlayPermissions.put(card.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(targetName + " exiles ")
                    .card(card)
                    .text(" from the top of their library — " + controllerName
                            + " may play it this turn.")
                    .build());
        }
    }
}
