package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Nicol Bolas, God-Pharaoh +2: target opponent digs until a nonland; controller may cast it
 * without paying its mana cost until end of turn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        if (opponentId == null || !gameData.playerIds.contains(opponentId) || opponentId.equals(controllerId)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(opponentId);
        String opponentName = gameData.playerIdToName.get(opponentId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(opponentName + "'s library is empty — nothing to exile."));
            return;
        }

        Card nonland = null;
        int exiledCount = 0;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            exileService.exileCard(gameData, opponentId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                nonland = top;
                break;
            }
        }

        if (nonland == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    opponentName + " exiles " + exiledCount + " card(s) from the top of their library"
                            + " — no nonland card found."));
            log.info("Game {} - {} dug entire library ({} cards) with no nonland for {}",
                    gameData.id, opponentName, exiledCount, entry.getCard().getName());
            return;
        }

        gameData.exilePlayPermissions.put(nonland.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(nonland.getId());
        gameData.exilePlayWithoutPayingManaCost.add(nonland.getId());

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .text(opponentName + " exiles cards until ").card(nonland)
                .text(" — " + controllerName + " may cast it without paying its mana cost this turn.")
                .build());
        log.info("Game {} - {} dug {} card(s) into {}; {} may cast it free this turn",
                gameData.id, opponentName, exiledCount, nonland.getName(), controllerName);
    }
}
