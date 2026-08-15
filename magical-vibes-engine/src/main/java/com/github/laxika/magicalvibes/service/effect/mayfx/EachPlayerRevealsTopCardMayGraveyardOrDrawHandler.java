package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardMayGraveyardOrDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerRevealsTopCardMayGraveyardOrDrawHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Autowired
    @Lazy
    private DrawService drawService;

    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardMayGraveyardOrDrawEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            for (UUID playerId : apnapOrder(gameData)) {
                List<Card> deck = gameData.playerDecks.get(playerId);
                if (deck == null || deck.isEmpty()) {
                    continue;
                }

                Card topCard = deck.removeFirst();
                graveyardService.addCardToGraveyard(gameData, playerId, topCard, Zone.LIBRARY);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(playerId) + " puts the revealed ", topCard,
                        " into their owner's graveyard."));
            }
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " puts the revealed cards into their owners' graveyards."));
        } else {
            for (UUID playerId : apnapOrder(gameData)) {
                drawService.resolveDrawCard(gameData, playerId);
            }
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " declines putting the revealed cards into graveyards; each player draws a card."));
        }

        log.info("Game {} - {} chooses {} for the revealed cards",
                gameData.id, player.getUsername(), accepted ? "graveyard" : "draw");
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
