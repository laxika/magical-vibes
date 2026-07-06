package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromOwnGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCardsFromOwnGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardsFromOwnGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCardsFromOwnGraveyardEffect) effect;

        UUID affectedPlayerId = e.affectedPlayerId();
        if (affectedPlayerId == null) {
            affectedPlayerId = entry.getControllerId();
        }
        int count = e.count();
        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(affectedPlayerId);

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = playerName + " has no cards in graveyard to exile.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no graveyard cards to exile", gameData.id, playerName);
            return;
        }

        if (graveyard.size() <= count) {
            // Auto-exile all cards
            List<Card> toExile = new ArrayList<>(graveyard);
            graveyard.clear();
            graveyardService.notifyCardsLeftGraveyard(gameData, affectedPlayerId);
            List<String> exiledNames = new ArrayList<>();
            for (Card card : toExile) {
                exileService.exileCard(gameData, affectedPlayerId, card);
                exiledNames.add(card.getName());
            }
            String logEntry = playerName + " exiles " + String.join(", ", exiledNames) + " from their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} auto-exiles {} cards from graveyard", gameData.id, playerName, toExile.size());
        } else {
            // Player must choose which cards to exile
            graveyardReturnSupport.beginGraveyardExileChoice(gameData, affectedPlayerId, count);
        }
    }
}
