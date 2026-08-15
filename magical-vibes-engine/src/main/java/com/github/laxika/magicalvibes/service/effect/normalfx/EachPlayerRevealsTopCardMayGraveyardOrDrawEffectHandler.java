package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardMayGraveyardOrDrawEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerRevealsTopCardMayGraveyardOrDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardMayGraveyardOrDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();
        String controllerName = gameData.playerIdToName.get(entry.getControllerId());

        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + "'s library is empty; no card is revealed (" + sourceName + ")."));
            } else {
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " reveals ", deck.getFirst(), " from the top of their library (" + sourceName + ")."));
            }
        }

        String prompt = "Put the revealed cards into their owners' graveyards? If you don't, each player draws a card. ("
                + controllerName + " — " + sourceName + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new EachPlayerRevealsTopCardMayGraveyardOrDrawEffect()),
                prompt,
                null,
                null,
                entry.getSourcePermanentId()
        ));
        playerInputService.processNextMayAbility(gameData);

        log.info("Game {} - {} reveals the top card for each player and prompts for graveyard or draw",
                gameData.id, sourceName);
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        order.add(gameData.activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
