package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        var library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        for (int i = 0; i < exileEffect.count() && !library.isEmpty(); i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            if (gameData.findExiledCard(card.getId()) == null) {
                continue;
            }

            gameData.exilePlayPermissions.put(card.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            if (card.getHandActivatedAbilities().stream()
                    .anyMatch(ActivatedAbility::isSuspendsSourceFromHand)) {
                gameData.exiledCardTimeCounters.put(card.getId(), exileEffect.timeCounters());
            }

            gameLogService.append(gameData, GameLog.builder()
                    .text(controllerName + " exiles ")
                    .card(card)
                    .text(" from the top of their library (may play it this turn).")
                    .build());
        }

        log.info("Game {} - {} exiled up to {} cards from library top and suspended matching cards",
                gameData.id, controllerName, exileEffect.count());
    }
}
