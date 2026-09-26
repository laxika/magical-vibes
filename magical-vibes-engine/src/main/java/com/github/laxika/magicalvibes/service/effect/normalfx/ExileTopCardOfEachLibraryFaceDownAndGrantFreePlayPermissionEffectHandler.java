package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachLibraryFaceDownAndGrantFreePlayPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Extract Power's library-top exile effect. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachLibraryFaceDownAndGrantFreePlayPermissionEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachLibraryFaceDownAndGrantFreePlayPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        int exiledCount = 0;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card topCard = library.removeFirst();
            exileService.exileCardFaceDown(gameData, playerId, topCard, null, controllerId);
            gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
            gameData.exilePlayWithoutPayingManaCost.add(topCard.getId());
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " looks at and exiles a card face down from "
                            + gameData.playerIdToName.get(playerId) + "'s library with " + sourceName + "."));
            exiledCount++;
        }

        log.info("Game {} - {} exiles {} top library card(s) face down with free play permission",
                gameData.id, sourceName, exiledCount);
    }
}
