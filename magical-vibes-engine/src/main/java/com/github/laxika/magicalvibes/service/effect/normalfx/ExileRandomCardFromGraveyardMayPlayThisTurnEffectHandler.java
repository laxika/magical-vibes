package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromGraveyardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomCardFromGraveyardMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardFromGraveyardMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (graveyard == null || graveyard.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " has no cards in their graveyard to exile."));
            return;
        }

        Card exiled = graveyard.get(ThreadLocalRandom.current().nextInt(graveyard.size()));
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiled.getId());
        exileService.exileCard(gameData, controllerId, exiled);

        gameData.exilePlayPermissions.put(exiled.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiled.getId());

        gameLogService.append(gameData, GameLog.builder()
                .text(controllerName + " exiles ").card(exiled)
                .text(" at random from their graveyard and may play it this turn.").build());
        log.info("Game {} - {} exiles {} at random from their graveyard and may play it this turn",
                gameData.id, controllerName, exiled.getName());
    }
}
