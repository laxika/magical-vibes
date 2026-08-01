package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = ((ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect) effect).count();
        if (count <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        int exiled = 0;
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            // Face-down: opponents do not see the card's identity; the controller may look
            // (and play via exilePlayPermissions) for as long as it remains exiled.
            exileService.exileCardFaceDown(gameData, controllerId, topCard, null);
            gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
            gameData.queueDelayedAction(new ExileToOwnerGraveyardAtNextUpkeep(
                    controllerId, topCard.getId(), controllerId, entry.getCard()));
            exiled++;
        }

        gameLogService.append(gameData, GameLog.text(
                controllerName + " exiles the top " + exiled
                        + " card(s) of their library face down (may play until next upkeep; "
                        + "unplayed cards go to the graveyard then)."));
        log.info("Game {} - {} exiles {} cards face down from library top (may play until next upkeep)",
                gameData.id, controllerName, exiled);
    }
}
