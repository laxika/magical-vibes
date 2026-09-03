package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles a landfall-style library dig that grants normal-cost cast permission for the first
 * nonland card found until end of turn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandMayCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandMayCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card nonland = null;
        int exiledCount = 0;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                nonland = top;
                break;
            }
        }

        if (nonland == null) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " exiles " + exiledCount + " card(s) from the top of their library"
                            + " — no nonland card found (" + sourceName + ")."));
            log.info("Game {} - {} dug entire library ({} cards) with no nonland for {}",
                    gameData.id, controllerName, exiledCount, sourceName);
            return;
        }

        gameData.exilePlayPermissions.put(nonland.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(nonland.getId());

        gameLogService.append(gameData, GameLog.builder()
                .text(controllerName + " exiles cards until ").card(nonland)
                .text(" — may cast it at its normal cost this turn (" + sourceName + ").")
                .build());
        log.info("Game {} - {} dug {} card(s) into {}; {} may cast it this turn",
                gameData.id, controllerName, exiledCount, nonland.getName(), controllerName);
    }
}
