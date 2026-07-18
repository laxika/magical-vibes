package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPlayFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillControllerAndMayPlayFromGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayPlayFromGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = controllerName + "'s library is empty — nothing to mill.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        Card milledCard = deck.getFirst();
        graveyardService.resolveMillPlayer(gameData, controllerId, 1);

        gameData.graveyardPlayPermissions.put(milledCard.getId(), controllerId);
        gameData.graveyardPlayPermissionsExpireEndOfTurn.add(milledCard.getId());

        String logEntry = controllerName + " mills " + milledCard.getName()
                + " and may play it from their graveyard this turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(controllerName + " mills ").card(milledCard).text(" and may play it from their graveyard this turn.").build());
        log.info("Game {} - {} mills {} and may play it from graveyard this turn",
                gameData.id, controllerName, milledCard.getName());
    }
}
